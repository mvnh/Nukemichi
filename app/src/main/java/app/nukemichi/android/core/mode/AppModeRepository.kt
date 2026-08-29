package app.nukemichi.android.core.mode

import kotlinx.coroutines.flow.StateFlow

interface AppModeRepository {
    val mode: StateFlow<AppMode>
    fun setMode(mode: AppMode)
}
