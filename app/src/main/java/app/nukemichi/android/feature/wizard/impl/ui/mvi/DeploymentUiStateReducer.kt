package app.nukemichi.android.feature.wizard.impl.ui.mvi

import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentStep
import app.nukemichi.android.feature.wizard.impl.domain.model.redactSecrets
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList

private const val MAX_LOG_LINES = 500

internal fun DeploymentUiState.reduce(event: DeploymentEvent): DeploymentUiState = when (event) {
    is DeploymentEvent.StepStarted -> withStepStatus(event.step, StepStatus.Running)
        .copy(phase = DeploymentPhase.InProgress)

    // logLines is always actually a PersistentList (it's only ever built by persistentListOf()/
    // this same mutate) — cast to reach add()/removeAt() without copying the whole buffer, which
    // ImmutableList's own declared type doesn't expose.
    is DeploymentEvent.LogLine -> copy(
        logLines = (logLines as PersistentList<String>).mutate { lines ->
            lines.add(redactSecrets(event.line))
            if (lines.size > MAX_LOG_LINES) lines.removeAt(0)
        }
    )

    is DeploymentEvent.StepSucceeded -> withStepStatus(event.step, StepStatus.Success)

    is DeploymentEvent.StepFailed -> withStepStatus(event.step, StepStatus.Failed)
        .copy(phase = DeploymentPhase.Failed(UiText.Raw(event.error.message ?: event.step.name)))

    is DeploymentEvent.Failed -> copy(phase = DeploymentPhase.Failed(UiText.Raw(event.error.message.orEmpty())))

    is DeploymentEvent.Completed -> copy(phase = DeploymentPhase.Succeeded(event.credentials))
}

private fun DeploymentUiState.withStepStatus(step: DeploymentStep, status: StepStatus): DeploymentUiState =
    copy(steps = steps.map { if (it.step == step) it.copy(status = status) else it }.toPersistentList())
