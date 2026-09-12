package app.nukemichi.android.feature.settings.impl.ui.mvi

import app.nukemichi.android.platform.mode.AppMode

internal object SettingsContract {

    data class State(
        val mode: AppMode = AppMode.NORMAL,
    )

    sealed interface Intent {
        data class AdvancedModeToggled(val enabled: Boolean) : Intent
        data object ViewLogsRequested : Intent
    }

    sealed interface Effect {
        data object NavigateToAdvancedModeIntro : Effect
        data object NavigateToLogs : Effect
    }
}
