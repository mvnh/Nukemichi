package app.nukemichi.android.platform.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@Stable
abstract class MviViewModel<State, Intent, Effect>(
    initialState: State
) : ViewModel(), ViewModelContext<State, Effect> {
    private val mutableState = MutableStateFlow(initialState)
    override val state: StateFlow<State> = mutableState.asStateFlow()

    override val scope: CoroutineScope = viewModelScope

    // UNLIMITED, matching intents: a bounded channel drops once its 64 slots fill, and none of
    // what fills them is droppable (navigation, a permission request, a share sheet). The collector
    // is gated on STARTED, so a backgrounded screen legitimately accumulates a backlog.
    private val effects = Channel<Effect>(Channel.UNLIMITED)
    val effect: Flow<Effect> = effects.receiveAsFlow()

    private val intents = Channel<Intent>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (intent in intents) {
                // One coroutine drains the channel, so an exception escaping a handler would end
                // the loop for the view model's whole life: later intents land in a channel nothing
                // reads and the screen stops responding with no crash to point at. Cancellation
                // still propagates, since that is the scope shutting this down on purpose.
                try {
                    onIntent(intent)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Throwable) {
                    Timber.e(error, "Unhandled failure while processing %s", intent)
                }
            }
        }
    }

    fun processIntent(intent: Intent) {
        intents.trySend(intent)
    }

    protected abstract suspend fun onIntent(intent: Intent)

    override fun reduce(action: State.() -> State) {
        mutableState.update { action(it) }
    }

    override fun sendEffect(effect: Effect) {
        val result = effects.trySend(effect)
        if (result.isFailure) {
            Timber.w(result.exceptionOrNull(), "Dropped effect %s: the channel is full or closed", effect)
        }
    }

    protected fun attachDelegates(vararg delegates: ViewModelDelegate<State, Effect>) {
        delegates.forEach { it.attach(this) }
    }
}
