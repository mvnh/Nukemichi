package app.nukemichi.android.feature.wizard.impl.ui.mvi

import app.nukemichi.android.R
import app.nukemichi.android.core.ssh.model.SshUntrustedHostException
import app.nukemichi.android.core.ui.mvi.ViewModelDelegate
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.feature.wizard.impl.domain.WizardSetupCoordinator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

internal class ConnectionCheckDelegate @Inject constructor(
    private val coordinator: WizardSetupCoordinator,
) : ViewModelDelegate<WizardContract.State, WizardContract.Effect>() {

    private var job: Job? = null

    fun validate() {
        job?.cancel()
        job = scope.launch {
            reduce { copy(connectionCheck = ConnectionCheckState.Checking) }
            val graceJob = launch {
                delay(GRACE_PERIOD_MS.milliseconds)
                reduce { copy(connectionCheck = ConnectionCheckState.StillChecking) }
            }
            val sshConfig = currentState.toSshConfigOrNull()
            if (sshConfig == null) {
                graceJob.cancel()
                reduce {
                    copy(
                        connectionCheck = ConnectionCheckState.Failed(
                            UiText.Resource(R.string.wizard_error_incomplete_ssh_details)
                        )
                    )
                }
                return@launch
            }
            coordinator.validateConnection(sshConfig, currentState.toSshAuth())
                .onSuccess { architecture ->
                    graceJob.cancel()
                    reduce { copy(connectionCheck = ConnectionCheckState.Idle, serverArchitecture = architecture) }
                    sendEffect(WizardContract.Effect.GoToNextPage)
                }
                .onFailure { error ->
                    graceJob.cancel()
                    // sshj wraps whatever the HostKeyVerifier throws as the *cause* of its own
                    // TransportException (confirmed in KeyExchanger.verifyHost's bytecode) rather
                    // than propagating it directly, so it has to be unwrapped here to tell
                    // "first connection to this host" apart from a real failure.
                    val untrusted = generateSequence(error) { it.cause }
                        .filterIsInstance<SshUntrustedHostException>()
                        .firstOrNull()
                    reduce {
                        copy(
                            connectionCheck = if (untrusted != null) {
                                ConnectionCheckState.UntrustedHost(untrusted.fingerprint)
                            } else {
                                ConnectionCheckState.Failed(
                                    error.message?.let(UiText::Raw) ?: UiText.Resource(R.string.wizard_error_unknown)
                                )
                            }
                        )
                    }
                }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        reduce { copy(connectionCheck = ConnectionCheckState.Idle) }
    }

    private companion object {
        const val GRACE_PERIOD_MS = 5_000L
    }
}
