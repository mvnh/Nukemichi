package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.internal.XrayTelemetryMonitor
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [XrayTelemetryMonitor]'s traffic-stats parsing/accumulation and its side-channel
 * [XrayTelemetryMonitor.healthDegraded] signal: the parts of the class [XrayTelemetryMonitorTest]
 * deliberately leaves untouched to stay focused on the state machine. `queryAllOutboundTrafficStats()`
 * returns a *delta* since the previous call (see the class's own comment on `uplinkTotalBytes`), so
 * these tests drive it through a scripted [XrayStatsSource] rather than asserting on the raw string
 * format directly.
 */
class XrayTelemetryMonitorStatsTest {

    @Test
    fun `parses a single poll's delta into both rate and running totals`() = runBlocking {
        val statsSource = ScriptedStatsSource(
            "proxy>>>outbound>>>traffic>>>uplink,uplink,1000;proxy>>>outbound>>>traffic>>>downlink,downlink,2000"
        )
        val monitor = XrayTelemetryMonitor(statsSource, Dispatchers.IO)

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        val first = monitor.stats.first()
        monitor.stopping()

        assertEquals(1000L, first.uplinkTotalBytes)
        assertEquals(2000L, first.downlinkTotalBytes)
        // delta / (pollIntervalMillis in seconds): 1000 bytes over a 250ms poll is 4000 B/s.
        assertEquals(4000L, first.uplinkBytesPerSecond)
        assertEquals(8000L, first.downlinkBytesPerSecond)
    }

    @Test
    fun `running totals accumulate delta over delta across polls`() = runBlocking {
        val statsSource = ScriptedStatsSource(
            "outbound,uplink,100;outbound,downlink,50",
            "outbound,uplink,40;outbound,downlink,10",
        )
        val monitor = XrayTelemetryMonitor(statsSource, Dispatchers.IO)

        val emitted = mutableListOf<XrayTrafficStats>()
        val collector = launch { monitor.stats.take(2).toList(emitted) }

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        collector.join()
        monitor.stopping()

        assertEquals(100L, emitted[0].uplinkTotalBytes)
        assertEquals(50L, emitted[0].downlinkTotalBytes)
        assertEquals(140L, emitted[1].uplinkTotalBytes)
        assertEquals(60L, emitted[1].downlinkTotalBytes)
    }

    @Test
    fun `totals reset when a fresh session starts running again`() = runBlocking {
        // Session two's own poll (200,300) is scripted to be smaller than session one's (500,700)
        // in *both* directions specifically so a leftover, un-reset total (500+200, 700+300) can't
        // be mistaken for either session's own number by coincidence.
        val statsSource = ScriptedStatsSource(
            "outbound,uplink,500;outbound,downlink,700",
            "outbound,uplink,200;outbound,downlink,300",
        )
        val monitor = XrayTelemetryMonitor(statsSource, Dispatchers.IO)

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        monitor.stats.first() // let the first session accumulate a nonzero total
        monitor.stopping()

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        val freshSessionFirst = monitor.stats.first()
        monitor.stopping()

        assertEquals(200L, freshSessionFirst.uplinkTotalBytes)
        assertEquals(300L, freshSessionFirst.downlinkTotalBytes)
    }

    @Test
    fun `an entry for an unrecognized direction is ignored rather than counted`() = runBlocking {
        val statsSource = ScriptedStatsSource("outbound,uplink,100;outbound,unknown,999")
        val monitor = XrayTelemetryMonitor(statsSource, Dispatchers.IO)

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        val first = monitor.stats.first()
        monitor.stopping()

        assertEquals(100L, first.uplinkTotalBytes)
        assertEquals(0L, first.downlinkTotalBytes)
    }

    @Test
    fun `a null poll result is skipped rather than emitted as a zeroed reading`() = runBlocking {
        val statsSource = ScriptedStatsSource(null, "outbound,uplink,10;outbound,downlink,5")
        val monitor = XrayTelemetryMonitor(statsSource, Dispatchers.IO)

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)
        // The only emission reaching a collector must be the second, non-null poll.
        val first = monitor.stats.first()
        monitor.stopping()

        assertEquals(10L, first.uplinkTotalBytes)
        assertEquals(5L, first.downlinkTotalBytes)
    }

    @Test
    fun `degraded signals healthDegraded without touching engine state`() = runBlocking {
        val monitor = XrayTelemetryMonitor(NoStatsSource, Dispatchers.IO)

        monitor.starting()
        monitor.running(POLL_INTERVAL_MS)

        val received = async { monitor.healthDegraded.first() }
        delay(50) // give the collector above a chance to actually suspend on the flow first
        monitor.degraded()
        received.await()

        assertEquals(XrayEngineState.RUNNING, monitor.state.value)
        monitor.stopping()
    }

    /** Returns each payload in order on successive calls, then repeats the last one. */
    private class ScriptedStatsSource(private vararg val payloads: String?) : XrayStatsSource {
        private val callIndex = AtomicInteger(0)

        override fun queryAllOutboundTrafficStats(): String? {
            val index = callIndex.getAndIncrement().coerceAtMost(payloads.lastIndex)
            return payloads[index]
        }
    }

    private object NoStatsSource : XrayStatsSource {
        override fun queryAllOutboundTrafficStats(): String? = null
    }

    private companion object {
        const val POLL_INTERVAL_MS = 250L
    }
}
