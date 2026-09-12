package app.nukemichi.android.feature.settings.impl.ui.mvi

import androidx.compose.runtime.Stable
import app.nukemichi.android.platform.mode.AppMode
import app.nukemichi.android.platform.mode.AppModeRepository
import app.nukemichi.android.platform.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val appModeRepository: AppModeRepository,
) : MviViewModel<SettingsContract.State, SettingsContract.Intent, SettingsContract.Effect>(
    SettingsContract.State(mode = appModeRepository.mode.value)
) {

    init {
        appModeRepository.mode
            .onEach { mode -> reduce { copy(mode = mode) } }
            .launchIn(scope)
    }

    override suspend fun onIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.AdvancedModeToggled -> if (intent.enabled) {
                sendEffect(SettingsContract.Effect.NavigateToAdvancedModeIntro)
            } else {
                appModeRepository.setMode(AppMode.NORMAL)
            }

            SettingsContract.Intent.ViewLogsRequested -> sendEffect(SettingsContract.Effect.NavigateToLogs)
        }
    }
}
