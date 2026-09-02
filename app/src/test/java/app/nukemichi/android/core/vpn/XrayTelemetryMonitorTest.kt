package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.internal.XrayTelemetryMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the monitor as its own state machine, driven purely through its public entry points:
 * the same ones [app.nukemichi.android.core.vpn.internal.NukemichiVpnService] drives it through,
 * now that state transitions come from `XrayRuntime.start()`/`stop()` succeeding or throwing
 * rather than from watching an external process.
 */
class XrayTelemetryMonitorTest {

    @Test
    fun `reaches RUNNING once started`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        assertEquals(XrayEngineState.STARTING, monitor.state.value)

        monitor.running(STATS_INTERVAL_MS)
        assertEquals(XrayEngineState.RUNNING, monitor.state.value)

        monitor.stopping()
    }

    @Test
    fun `settles on STOPPED after stopping`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)
        monitor.stopping()

        assertEquals(XrayEngineState.STOPPED, monitor.state.value)
    }

    @Test
    fun `failed reports ERROR`() = runBlocking {
        val monitor = monitor()

        monitor.failed(IllegalArgumentException("missing configuration"))

        assertEquals(XrayEngineState.ERROR, monitor.state.value)
    }

    @Test
    fun `refuses to track a second running session while one is already tracked`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)

        val failure = runCatching { monitor.running(STATS_INTERVAL_MS) }
        assertTrue(
            "A second running() must not silently replace the tracked session",
            failure.exceptionOrNull() is IllegalStateException
        )

        monitor.stopping()
    }

    @Test
    fun `a fresh session after stopping is tracked normally, not rejected`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)
        monitor.stopping()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)
        assertEquals(XrayEngineState.RUNNING, monitor.state.value)

        monitor.stopping()
    }

    private fun monitor() = XrayTelemetryMonitor(
        statsSource = NoStatsSource,
        ioDispatcher = Dispatchers.IO,
    )

    private object NoStatsSource : XrayStatsSource {
        override fun queryAllOutboundTrafficStats(): String? = null
    }

    private companion object {
        const val STATS_INTERVAL_MS = 60_000L
    }
}
