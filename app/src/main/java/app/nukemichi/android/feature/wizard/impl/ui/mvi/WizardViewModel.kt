package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.compose.runtime.Stable
import app.nukemichi.android.R
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.feature.wizard.impl.domain.WizardSetupCoordinator
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Effect
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Intent
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.State
import app.nukemichi.android.platform.mode.AppModeRepository
import app.nukemichi.android.platform.ui.mvi.MviViewModel
import app.nukemichi.android.platform.ui.util.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Stable
@HiltViewModel(assistedFactory = WizardViewModel.Factory::class)
internal class WizardViewModel @AssistedInject constructor(
    // The subscription a deployed server joins; null puts it in a new one.
    @Assisted private val targetSubscriptionId: String?,
    private val coordinator: WizardSetupCoordinator,
    private val xrayControl: XrayControl,
    private val appStorage: AppStorage,
    private val appModeRepository: AppModeRepository,
    private val connectionCheckDelegate: ConnectionCheckDelegate,
    private val deploymentDelegate: DeploymentDelegate,
) : MviViewModel<State, Intent, Effect>(
    initialState = State()
) {
    init {
        attachDelegates(connectionCheckDelegate, deploymentDelegate)
        appModeRepository.mode
            .onEach { mode -> reduce { copy(appMode = mode) } }
            .launchIn(scope)
    }

    override suspend fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.SetupStrategyChanged -> reduce { copy(setupStrategy = intent.strategy) }
            is Intent.ServerAuthMethodChanged -> reduce { copy(serverAuthMethod = intent.method) }
            is Intent.ServerAddressChanged -> reduce { copy(serverAddress = intent.address) }
            is Intent.PasswordChanged -> reduce { copy(password = intent.password) }
            is Intent.SshKeyChanged -> reduce { copy(sshKey = intent.sshKey) }
            is Intent.SshPortChanged -> reduce { copy(sshPort = intent.port) }
            is Intent.UsernameChanged -> reduce { copy(username = intent.username) }
            is Intent.SshFingerprintChanged -> reduce { copy(sshFingerprint = intent.fingerprint) }
            is Intent.OnCloseWizardClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnNextClicked -> handleNextClick(intent.currentPageIdx)
            Intent.CancelConnectionCheck -> connectionCheckDelegate.cancel()
            Intent.DismissConnectionErrorDialog -> reduce { copy(connectionCheck = ConnectionCheckState.Idle) }
            is Intent.TrustHostAndRetry -> {
                reduce { copy(sshFingerprint = intent.fingerprint) }
                connectionCheckDelegate.validate()
            }
            Intent.RetryDeployment -> deploymentDelegate.start()
            Intent.CancelDeployment -> deploymentDelegate.cancel()
            Intent.ToggleTerminalVisibility -> reduce {
                copy(deployment = deployment.copy(isTerminalExpanded = !deployment.isTerminalExpanded))
            }
            Intent.FinishAndStartVpn -> finishSetup()
            Intent.VpnPermissionGranted -> startVpn()
            Intent.VpnPermissionDenied ->
                reduce { copy(errorMessage = UiText.Resource(R.string.wizard_error_vpn_permission_required)) }
        }
    }

    private fun handleNextClick(currentPageIdx: Int) {
        when (currentPageIdx) {
            0 -> sendEffect(Effect.GoToNextPage)
            1 -> connectionCheckDelegate.validate()
            2 -> {
                sendEffect(Effect.GoToNextPage)
                deploymentDelegate.start()
            }
        }
    }

    private suspend fun finishSetup() {
        coordinator.saveProfile(state.value.toProfileDraft(), targetSubscriptionId)
            .onSuccess { profile ->
                appStorage.putBoolean(StorageDomain.EXPERIENCE, ExperienceKeys.WIZARD_COMPLETED, true)
                if (xrayControl.needsVpnPermission()) sendEffect(Effect.RequestVpnPermission)
                else startVpn(profile)
            }
            .onFailure { error ->
                Timber.e(error, "Saving the deployed profile failed")
                reduce { copy(errorMessage = UiText.Resource(R.string.wizard_error_save_failed)) }
            }
    }

    private suspend fun startVpn(savedProfile: XrayVpnProfile? = null) {
        val profile = savedProfile
            ?: coordinator.saveProfile(state.value.toProfileDraft(), targetSubscriptionId).getOrElse { error ->
                Timber.e(error, "Saving the deployed profile failed")
                reduce { copy(errorMessage = UiText.Resource(R.string.wizard_error_save_failed)) }
                return
            }
        reduce { copy(isLoading = true, errorMessage = null) }
        processStartResult(profile, xrayControl.start(XrayClientConfigFactory.createRuntimeConfig(profile)))
    }

    private fun processStartResult(profile: XrayVpnProfile, result: Result<Unit>) {
        result.onSuccess {
            reduce { copy(isLoading = false, errorMessage = null) }
            sendEffect(Effect.NavigateToDashboard(selectServerId = profile.id))
        }.onFailure { error ->
            Timber.e(error, "Starting the VPN from the wizard failed")
            reduce { copy(isLoading = false, errorMessage = UiText.Resource(R.string.wizard_error_start_failed)) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(targetSubscriptionId: String?): WizardViewModel
    }
}
