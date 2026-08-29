package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.compose.runtime.Stable
import app.nukemichi.android.R
import app.nukemichi.android.core.mode.AppModeRepository
import app.nukemichi.android.core.security.Secret
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.core.ui.mvi.PatternViewModel
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.feature.wizard.impl.domain.WizardSetupCoordinator
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Effect
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.Intent
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
@HiltViewModel
internal class WizardViewModel @Inject constructor(
    private val coordinator: WizardSetupCoordinator,
    private val xrayControl: XrayControl,
    private val appStorage: AppStorage,
    private val appModeRepository: AppModeRepository,
    private val connectionCheckDelegate: ConnectionCheckDelegate,
    private val deploymentDelegate: DeploymentDelegate,
) : PatternViewModel<State, Intent, Effect>(
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
            is Intent.PasswordChanged -> reduce { copy(password = Secret(intent.password)) }
            is Intent.SshKeyChanged -> reduce { copy(sshKey = Secret(intent.sshKey)) }
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
            is Intent.AcknowledgeRisksToggled -> reduce { copy(hasAcknowledgedRisks = intent.checked) }
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
        coordinator.saveProfile(state.value.toProfileDraft())
            .onSuccess { profile ->
                appStorage.putString(StorageDomain.EXPERIENCE, ExperienceKeys.WIZARD_COMPLETED, "true")
                if (xrayControl.needsVpnPermission()) sendEffect(Effect.RequestVpnPermission)
                else startVpn(profile)
            }
            .onFailure { error -> reduce { copy(errorMessage = error.message?.let(UiText::Raw)) } }
    }

    private suspend fun startVpn(savedProfile: XrayVpnProfile? = null) {
        val profile = savedProfile ?: coordinator.saveProfile(state.value.toProfileDraft()).getOrElse { error ->
            reduce { copy(errorMessage = error.message?.let(UiText::Raw)) }
            return
        }
        reduce { copy(isLoading = true, errorMessage = null) }
        processStartResult(xrayControl.start(XrayClientConfigFactory.createRuntimeConfig(profile)))
    }

    private suspend fun processStartResult(result: Result<Unit>) {
        result.onSuccess {
            reduce { copy(isLoading = false, errorMessage = null) }
            sendEffect(Effect.NavigateToDashboard)
        }.onFailure { error -> reduce { copy(isLoading = false, errorMessage = error.message?.let(UiText::Raw)) } }
    }
}
