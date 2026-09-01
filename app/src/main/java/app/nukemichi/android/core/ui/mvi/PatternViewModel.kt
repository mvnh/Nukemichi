package app.nukemichi.android.core.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
abstract class PatternViewModel<State, Intent, Effect>(
    initialState: State
) : ViewModel(), ViewModelContext<State, Effect> {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state.asStateFlow()

    override val scope: CoroutineScope = viewModelScope

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    private val _intents = Channel<Intent>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (intent in _intents) {
                onIntent(intent)
            }
        }
    }

    fun processIntent(intent: Intent) {
        _intents.trySend(intent)
    }

    protected abstract suspend fun onIntent(intent: Intent)

    override fun reduce(action: State.() -> State) {
        _state.update { action(it) }
    }

    override fun sendEffect(effect: Effect) {
        _effect.trySend(effect).onFailure { error ->
            Timber.w(error, "Dropped effect %s: channel full or closed", effect)
        }
    }

    protected fun attachDelegates(vararg delegates: ViewModelDelegate<State, Effect>) {
        delegates.forEach { it.attach(this) }
    }
}
