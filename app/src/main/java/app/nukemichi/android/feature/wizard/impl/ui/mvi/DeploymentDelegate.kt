package app.nukemichi.android.feature.wizard.impl.ui.mvi

import app.nukemichi.android.core.ui.mvi.ViewModelDelegate
import app.nukemichi.android.feature.wizard.impl.domain.WizardSetupCoordinator
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DeploymentDelegate @Inject constructor(
    private val coordinator: WizardSetupCoordinator,
) : ViewModelDelegate<WizardContract.State, WizardContract.Effect>() {

    private var job: Job? = null

    fun start() {
        val architecture = currentState.serverArchitecture ?: return
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
}
