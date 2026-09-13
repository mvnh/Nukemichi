package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.annotation.StringRes
import app.nukemichi.android.R
import app.nukemichi.android.feature.wizard.impl.domain.WizardSetupCoordinator
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import app.nukemichi.android.platform.ui.mvi.ViewModelDelegate
import app.nukemichi.android.platform.ui.util.UiText
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

internal class DeploymentDelegate @Inject constructor(
    private val coordinator: WizardSetupCoordinator,
) : ViewModelDelegate<WizardContract.State, WizardContract.Effect>() {

    private var job: Job? = null

    fun start() {
        // Both used to leave the user on a deployment page that never started. Neither is
        // recoverable from here, so say so on the page itself.
        if (currentState.setupStrategy != WizardContract.SetupStrategy.FAST_START) {
            failBeforeStarting(R.string.wizard_error_strategy_unsupported)
            return
        }
        val architecture = currentState.serverArchitecture ?: run {
            failBeforeStarting(R.string.wizard_error_architecture_unknown)
            return
        }
        job?.cancel()
        reduce { copy(deployment = DeploymentUiState()) }
        job = scope.launch {
            val sshConfig = currentState.toSshConfigOrNull()
            val events = if (sshConfig == null) {
                flowOf(DeploymentEvent.Failed(IllegalArgumentException("Complete the SSH connection details first.")))
            } else {
                coordinator.deploy(sshConfig, currentState.toSshAuth(), architecture)
            }
            events.collect { event ->
                reduce {
                    val next = copy(deployment = deployment.reduce(event))
                    if (event is DeploymentEvent.Completed) {
                        next.copy(
                            uuid = event.credentials.uuid,
                            realityPublicKey = event.credentials.publicKey,
                            realityShortId = event.credentials.shortId,
                            realityServerName = event.credentials.realityServerName,
                            serverCountryCode = event.credentials.countryCode,
                        )
                    } else {
                        next
                    }
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        reduce { copy(deployment = DeploymentUiState()) }
    }

    private fun failBeforeStarting(@StringRes reason: Int) {
        job?.cancel()
        job = null
        reduce {
            copy(deployment = DeploymentUiState(phase = DeploymentPhase.Failed(UiText.Resource(reason))))
        }
    }
}
