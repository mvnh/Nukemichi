package app.nukemichi.android.feature.wizard.impl.domain.model

internal sealed interface DeploymentEvent {
    data class StepStarted(val step: DeploymentStep) : DeploymentEvent
    data class LogLine(val step: DeploymentStep, val line: String) : DeploymentEvent
    data class StepSucceeded(val step: DeploymentStep) : DeploymentEvent
    data class StepFailed(val step: DeploymentStep, val error: Throwable) : DeploymentEvent
    data class Completed(val credentials: XrayServerCredentials) : DeploymentEvent

    /** A failure not tied to any step, e.g. the SSH session itself couldn't be (re)opened. */
    data class Failed(val error: Throwable) : DeploymentEvent
}
