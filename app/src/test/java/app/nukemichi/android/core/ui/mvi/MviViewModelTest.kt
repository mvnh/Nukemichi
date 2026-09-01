package app.nukemichi.android.core.ui.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * `onIntent` is suspending, and implementations read `state.value`, mutate, then suspend on I/O.
 * Concurrent processing would interleave that read-modify-write and lose updates — only under fast
 * input, and near-impossible to reproduce by hand.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `intents are processed one at a time and in order`() = runTest(dispatcher) {
        val viewModel = TestViewModel()

        repeat(50) { index -> viewModel.processIntent(TestIntent.Append(index)) }
        advanceUntilIdle()

        assertEquals((0 until 50).toList(), viewModel.state.value.seen)
    }

    @Test
    fun `a suspending intent does not let the next one interleave`() = runTest(dispatcher) {
        val viewModel = TestViewModel(suspendMidway = true)

        repeat(20) { index -> viewModel.processIntent(TestIntent.Append(index)) }
        advanceUntilIdle()

        assertEquals((0 until 20).toList(), viewModel.state.value.seen)
    }

    @Test
    fun `reduce applies to the latest state, not a captured snapshot`() = runTest(dispatcher) {
        val viewModel = TestViewModel()

        viewModel.processIntent(TestIntent.Increment)
        viewModel.processIntent(TestIntent.Increment)
        viewModel.processIntent(TestIntent.Increment)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.counter)
    }

    @Test
    fun `effects are delivered to a collector in order`() = runTest(dispatcher) {
        val viewModel = TestViewModel()
        val effects = mutableListOf<TestEffect>()

        val collection = backgroundScope.launch { viewModel.effect.take(2).toList(effects) }
        advanceUntilIdle()

        viewModel.processIntent(TestIntent.EmitEffect("first"))
        viewModel.processIntent(TestIntent.EmitEffect("second"))
        advanceUntilIdle()
        collection.join()

        assertEquals(listOf(TestEffect("first"), TestEffect("second")), effects)
    }

    /** Normal during a recreation: the channel is buffered, not conflated. */
    @Test
    fun `effects raised before anyone is collecting are not lost`() = runTest(dispatcher) {
        val viewModel = TestViewModel()

        viewModel.processIntent(TestIntent.EmitEffect("early"))
        advanceUntilIdle()

        val effects = mutableListOf<TestEffect>()
        val collection = backgroundScope.launch { viewModel.effect.take(1).toList(effects) }
        advanceUntilIdle()
        collection.join()

        assertEquals(listOf(TestEffect("early")), effects)
    }

    @Test
    fun `an attached delegate reduces into the same state as the view model`() = runTest(dispatcher) {
        val delegate = CountingDelegate()
        val viewModel = TestViewModel(delegate = delegate)
        advanceUntilIdle()

        delegate.bump()

        assertEquals(1, viewModel.state.value.counter)
    }

    @Test
    fun `an unattached delegate fails with a usable message rather than an NPE`() {
        val error = assertThrows(IllegalStateException::class.java) { CountingDelegate().bump() }

        assertTrue("unhelpful message: ${error.message}", error.message.orEmpty().contains("attach"))
    }
}

private data class TestState(val counter: Int = 0, val seen: List<Int> = emptyList())

private sealed interface TestIntent {
    data class Append(val value: Int) : TestIntent
    data object Increment : TestIntent
    data class EmitEffect(val label: String) : TestIntent
}

private data class TestEffect(val label: String)

private class TestViewModel(
    private val suspendMidway: Boolean = false,
    delegate: CountingDelegate? = null,
) : MviViewModel<TestState, TestIntent, TestEffect>(TestState()) {

    init {
        delegate?.let { attachDelegates(it) }
    }

    override suspend fun onIntent(intent: TestIntent) {
        when (intent) {
            is TestIntent.Append -> {
                val snapshot = state.value.seen
                if (suspendMidway) yield()
                reduce { copy(seen = snapshot + intent.value) }
            }

            TestIntent.Increment -> reduce { copy(counter = counter + 1) }
            is TestIntent.EmitEffect -> sendEffect(TestEffect(intent.label))
        }
    }
}

private class CountingDelegate : ViewModelDelegate<TestState, TestEffect>() {
    fun bump() = reduce { copy(counter = counter + 1) }
}
