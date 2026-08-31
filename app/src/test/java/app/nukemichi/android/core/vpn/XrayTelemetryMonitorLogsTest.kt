package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.internal.XrayTelemetryMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the `logs` flow, which neither [XrayTelemetryMonitorTest] (state machine) nor
 * [XrayTelemetryMonitorStatsTest] (traffic accounting) touches.
 *
 * It matters because this flow is the only channel through which a start failure reaches the user.
 * The engine state going ERROR just greys out a toggle; the log line is what says *why*, and it is
 * what gets copied out of the log screen when someone asks for help.
 *
 * `logs` is a replay-less SharedFlow, so — as in the sibling tests — each case subscribes first and
 * yields before emitting; an emission with no subscriber attached is dropped, not buffered.
 */
class XrayTelemetryMonitorLogsTest {

    @Test
    fun `a start failure is published as an ERROR log carrying the reason`() = runBlocking {
        val monitor = monitor()
        val received = async { monitor.logs.first() }
        delay(COLLECTOR_ATTACH_DELAY_MS)

        monitor.failed(IllegalStateException("Xray core reported success but is not running."))

        val log = received.await()
        assertEquals(XrayLogLevel.ERROR, log.level)
        assertEquals("Xray core reported success but is not running.", log.message)
    }

    /** A throwable with no message must still say something, or the log screen shows a blank row. */
    @Test
    fun `a failure without a message falls back to a readable one`() = runBlocking {
        val monitor = monitor()
        val received = async { monitor.logs.first() }
        delay(COLLECTOR_ATTACH_DELAY_MS)

        monitor.failed(RuntimeException())

        assertTrue(received.await().message.isNotBlank())
    }

    /**
     * The log screen orders by this sequence rather than by arrival, so a duplicate or a reset
     * would scramble exactly the lines someone is reading to diagnose a failure.
     */
    @Test
    fun `log sequence numbers strictly increase`() = runBlocking {
        val monitor = monitor()
        val received = async { monitor.logs.take(3).toList() }
        delay(COLLECTOR_ATTACH_DELAY_MS)

        repeat(3) { index -> monitor.failed(IllegalStateException("failure $index")) }

        val sequences = received.await().map { it.sequence }
        assertEquals("sequence numbers must not go backwards", sequences.sorted(), sequences)
        assertEquals("sequence numbers must be unique", sequences.size, sequences.toSet().size)
    }

    @Test
    fun `a failure moves the engine to ERROR as well as logging it`() = runBlocking {
        val monitor = monitor()

        monitor.failed(IllegalStateException("boom"))

        assertEquals(XrayEngineState.ERROR, monitor.state.value)
    }

    private fun monitor() = XrayTelemetryMonitor(NoStatsSource, Dispatchers.IO)

    private object NoStatsSource : XrayStatsSource {
        override fun queryAllOutboundTrafficStats(): String? = null
    }

    private companion object {
        /** Long enough for the collector above to actually suspend on the flow first. */
        const val COLLECTOR_ATTACH_DELAY_MS = 50L
    }
}
