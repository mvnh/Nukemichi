package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.compose.runtime.Immutable
import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.security.Secret
import app.nukemichi.android.core.ui.util.UiText

object WizardContract {

    enum class SetupStrategy { FAST_START, NAIVEPROXY }
    enum class ServerAuthMethod { PASSWORD, SSH_KEY }

    @Immutable
    internal data class State(
        val setupStrategy: SetupStrategy = SetupStrategy.FAST_START,
        val serverAuthMethod: ServerAuthMethod = ServerAuthMethod.PASSWORD,
        val serverAddress: String = "",
        val password: Secret = Secret(""),
        val sshKey: Secret = Secret(""),
        val sshPort: String = "22",
        val username: String = "root",
        val sshFingerprint: String = "",
        val uuid: String = "",
        val realityPublicKey: String = "",
        val realityShortId: String = "",
        val realityServerName: String = "",
        val serverArchitecture: String? = null,
        val connectionCheck: ConnectionCheckState = ConnectionCheckState.Idle,
        val hasAcknowledgedRisks: Boolean = false,
        val deployment: DeploymentUiState = DeploymentUiState(),
        val isLoading: Boolean = false,
        val errorMessage: UiText? = null,
        val appMode: AppMode = AppMode.NORMAL,
    )

    internal sealed interface Intent {
        data object OnCloseWizardClicked : Intent
        data class OnNextClicked(val currentPageIdx: Int) : Intent
        data class SetupStrategyChanged(val strategy: SetupStrategy) : Intent
        data class ServerAuthMethodChanged(val method: ServerAuthMethod) : Intent
        data class ServerAddressChanged(val address: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data class SshKeyChanged(val sshKey: String) : Intent
        data class SshPortChanged(val port: String) : Intent
        data class UsernameChanged(val username: String) : Intent
        data class SshFingerprintChanged(val fingerprint: String) : Intent
        data object CancelConnectionCheck : Intent
        data object DismissConnectionErrorDialog : Intent
        data class TrustHostAndRetry(val fingerprint: String) : Intent
        data class AcknowledgeRisksToggled(val checked: Boolean) : Intent
        data object RetryDeployment : Intent
        data object CancelDeployment : Intent
        data object ToggleTerminalVisibility : Intent
        data object FinishAndStartVpn : Intent
        data object VpnPermissionGranted : Intent
        data object VpnPermissionDenied : Intent
    }

    internal sealed interface Effect {
        data object GoToNextPage : Effect
        data object NavigateBack : Effect
        data object NavigateToDashboard : Effect
        data object RequestVpnPermission : Effect
    }

}

internal val WizardContract.State.isSshValid: Boolean
    get() = serverAddress.isNotBlank() && username.isNotBlank() &&
        sshPort.toIntOrNull() in 1..65_535 &&
        when (serverAuthMethod) {
            WizardContract.ServerAuthMethod.PASSWORD -> password.value.isNotBlank()
            WizardContract.ServerAuthMethod.SSH_KEY -> sshKey.value.isNotBlank()
        }

internal val WizardContract.State.isConnectionProfileValid: Boolean
    get() = UUID_PATTERN.matches(uuid) &&
        realityPublicKey.isNotBlank() && realityShortId.isNotBlank() && realityServerName.isNotBlank()

private val UUID_PATTERN = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
)
