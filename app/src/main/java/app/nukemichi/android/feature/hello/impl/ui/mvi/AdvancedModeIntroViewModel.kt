package app.nukemichi.android.feature.hello.impl.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.mode.AppModeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Stable
@HiltViewModel
internal class AdvancedModeIntroViewModel @Inject constructor(
    private val appModeRepository: AppModeRepository,
) : ViewModel() {

    fun confirmAdvancedMode() {
        appModeRepository.setMode(AppMode.ADVANCED)
    }
}
