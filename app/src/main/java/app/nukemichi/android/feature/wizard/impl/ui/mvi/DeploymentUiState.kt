package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.compose.runtime.Immutable
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentStep
import app.nukemichi.android.feature.wizard.impl.domain.model.XrayServerCredentials
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

internal enum class StepStatus { Pending, Running, Success, Failed }

@Immutable
internal data class DeploymentStepUi(
    val step: DeploymentStep,
    val status: StepStatus = StepStatus.Pending,
)

@Immutable
internal sealed interface DeploymentPhase {
    data object NotStarted : DeploymentPhase
    data object InProgress : DeploymentPhase
    data class Failed(val reason: UiText) : DeploymentPhase
    data class Succeeded(val credentials: XrayServerCredentials) : DeploymentPhase
}

@Immutable
internal data class DeploymentUiState(
    val steps: ImmutableList<DeploymentStepUi> = DeploymentStep.entries.map(::DeploymentStepUi).toPersistentList(),
    val phase: DeploymentPhase = DeploymentPhase.NotStarted,
    val logLines: PersistentList<String> = persistentListOf(),
    val isTerminalExpanded: Boolean = false,
)
