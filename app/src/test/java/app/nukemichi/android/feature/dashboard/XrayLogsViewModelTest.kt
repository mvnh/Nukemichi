package app.nukemichi.android.feature.dashboard

import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.core.vpn.XrayServiceProvider
import app.nukemichi.android.core.vpn.XrayTrafficStats
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.XrayLogsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class XrayLogsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val logs = MutableSharedFlow<XrayLogMessage>(extraBufferCapacity = 8192)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `renders the lines it was given`() = runTest(dispatcher) {
        val viewModel = XrayLogsViewModel(FakeServiceProvider(logs), dispatcher)
        advanceUntilIdle()

        logs.emit(message("tunnel up"))
        advanceUntilIdle()

        assertEquals(1, viewModel.lines.value.size)
        assertTrue(viewModel.lines.value.single().endsWith("tunnel up"))
    }

    /**
     * The whole reason the buffer exists. xray-core at debug level produces lines faster than
     * anything can render them, and the bound is what keeps memory flat across a long session.
     */
    @Test
    fun `keeps only the most recent lines`() = runTest(dispatcher) {
        val viewModel = XrayLogsViewModel(FakeServiceProvider(logs), dispatcher)
        advanceUntilIdle()

        repeat(MAX_LINES + 250) { index -> logs.emit(message("line $index")) }
        advanceUntilIdle()

        val lines = viewModel.lines.value
        assertEquals(MAX_LINES, lines.size)
        assertTrue("oldest lines must be evicted, got: ${lines.first()}", lines.first().endsWith("line 250"))
        assertTrue(
            "newest line must survive, got: ${lines.last()}",
            lines.last().endsWith("line ${MAX_LINES + 249}"),
        )
    }

    private fun message(text: String) = XrayLogMessage(level = 1, message = text, timestampMillis = 0L)

    private companion object {
        /** Stated here rather than read off the view model, so changing the bound has to be deliberate. */
        const val MAX_LINES = 1000
    }
}

private class FakeServiceProvider(override val monitoring: XrayMonitoring) : XrayServiceProvider {
    constructor(logs: Flow<XrayLogMessage>) : this(FakeMonitoring(logs))

    override val control: XrayControl = object : XrayControl {
        override fun needsVpnPermission() = false
        override suspend fun start(config: XrayRuntimeConfig) = Result.success(Unit)
        override suspend fun reload(config: XrayRuntimeConfig) = Result.success(Unit)
        override suspend fun stop() = Result.success(Unit)
    }
}

private class FakeMonitoring(override val logs: Flow<XrayLogMessage>) : XrayMonitoring {
    override val state: StateFlow<XrayEngineState> = MutableStateFlow(XrayEngineState.RUNNING)
    override val stats: Flow<XrayTrafficStats> = MutableSharedFlow()
    override val healthDegraded: Flow<Unit> = MutableSharedFlow()
    override val sessionServerId: StateFlow<String?> = MutableStateFlow(null)
    override val runningSinceRealtime: StateFlow<Long?> = MutableStateFlow(null)
}
