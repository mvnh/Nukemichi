package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.internal.ElapsedRealtimeSource
import app.nukemichi.android.core.vpn.internal.XrayTelemetryMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `reports the session server while running and clears it once stopped`() = runBlocking {
        val monitor = monitor()
        assertNull(monitor.sessionServerId.value)

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS, serverId = "server-a")
        assertEquals("server-a", monitor.sessionServerId.value)

        monitor.stopping()
        assertNull("a stopped engine must not claim a session server", monitor.sessionServerId.value)

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS, serverId = "server-b")
        assertEquals("a restart reports the server it switched to", "server-b", monitor.sessionServerId.value)

        monitor.stopping()
    }

    @Test
    fun `reports a running-since timestamp while running and clears it once stopped`() = runBlocking {
        val monitor = monitor()
        assertNull(monitor.runningSinceRealtime.value)

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)
        assertEquals(
            "the UI reads this instead of guessing its own start time on reconnect - see ConnectionDelegate",
            FAKE_ELAPSED_REALTIME_MS,
            monitor.runningSinceRealtime.value,
        )

        monitor.stopping()
        assertNull("a stopped engine must not claim a running-since time", monitor.runningSinceRealtime.value)
    }

    @Test
    fun `failing clears a running-since timestamp`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS)
        monitor.failed(IllegalStateException("tunnel lost"))

        assertNull(monitor.runningSinceRealtime.value)
    }

    @Test
    fun `failing clears a previously reported session server`() = runBlocking {
        val monitor = monitor()

        monitor.starting()
        monitor.running(STATS_INTERVAL_MS, serverId = "server-a")
        monitor.failed(IllegalStateException("tunnel lost"))

        assertNull(monitor.sessionServerId.value)
        monitor.stopping()
    }

    private fun monitor() = XrayTelemetryMonitor(
        statsSource = NoStatsSource,
        ioDispatcher = Dispatchers.IO,
        elapsedRealtimeSource = ElapsedRealtimeSource { FAKE_ELAPSED_REALTIME_MS },
    )

    private object NoStatsSource : XrayStatsSource {
        override fun queryAllOutboundTrafficStats(): String? = null
    }

    private companion object {
        const val STATS_INTERVAL_MS = 60_000L
        const val FAKE_ELAPSED_REALTIME_MS = 1_000_000L
    }
}
