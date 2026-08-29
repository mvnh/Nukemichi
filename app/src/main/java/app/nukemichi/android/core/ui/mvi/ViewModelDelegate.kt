package app.nukemichi.android.core.ui.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface ViewModelContext<State, Effect> {
    val state: StateFlow<State>
    val scope: CoroutineScope
    fun reduce(action: State.() -> State)
    fun sendEffect(effect: Effect)
}

abstract class ViewModelDelegate<State, Effect> {

    private var _context: ViewModelContext<State, Effect>? = null

    protected val context: ViewModelContext<State, Effect>
        get() = _context ?: error("Delegate not attached. Call attach(context) first.")

    fun attach(context: ViewModelContext<State, Effect>) {
        _context = context
    }

    protected val currentState: State
        get() = context.state.value

    protected fun reduce(update: State.() -> State) {
        context.reduce(update)
    }

    protected fun sendEffect(effect: Effect) {
        context.sendEffect(effect)
    }

    protected val scope: CoroutineScope
        get() = context.scope
}
