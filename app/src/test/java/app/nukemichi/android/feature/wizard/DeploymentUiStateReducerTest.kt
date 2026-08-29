package app.nukemichi.android.feature.wizard

import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentStep
import app.nukemichi.android.feature.wizard.impl.domain.model.XrayServerCredentials
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentPhase
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentUiState
import app.nukemichi.android.feature.wizard.impl.ui.mvi.StepStatus
import app.nukemichi.android.feature.wizard.impl.ui.mvi.reduce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeploymentUiStateReducerTest {

    private fun statusOf(state: DeploymentUiState, step: DeploymentStep): StepStatus =
        state.steps.first { it.step == step }.status

    @Test
    fun `step started marks the step running and the phase in progress`() {
        val state = DeploymentUiState().reduce(DeploymentEvent.StepStarted(DeploymentStep.INSTALL_RUNTIME))

        assertEquals(StepStatus.Running, statusOf(state, DeploymentStep.INSTALL_RUNTIME))
        assertEquals(DeploymentPhase.InProgress, state.phase)
    }

    @Test
    fun `a log line appends to the log buffer`() {
        val state = DeploymentUiState()
            .reduce(DeploymentEvent.LogLine(DeploymentStep.INSTALL_RUNTIME, "first"))
            .reduce(DeploymentEvent.LogLine(DeploymentStep.INSTALL_RUNTIME, "second"))

        assertEquals(listOf("first", "second"), state.logLines)
    }

    @Test
    fun `step succeeded marks only that step as success`() {
        val state = DeploymentUiState().reduce(DeploymentEvent.StepSucceeded(DeploymentStep.GENERATE_SECRETS))

        assertEquals(StepStatus.Success, statusOf(state, DeploymentStep.GENERATE_SECRETS))
        assertEquals(StepStatus.Pending, statusOf(state, DeploymentStep.INSTALL_RUNTIME))
    }

    @Test
    fun `step failed marks the step failed and sets the failure phase`() {
        val state = DeploymentUiState()
            .reduce(DeploymentEvent.StepFailed(DeploymentStep.START_SERVICE, IllegalStateException("boom")))

        assertEquals(StepStatus.Failed, statusOf(state, DeploymentStep.START_SERVICE))
        assertTrue(state.phase is DeploymentPhase.Failed)
    }

    @Test
    fun `completed sets the succeeded phase with credentials`() {
        val credentials = XrayServerCredentials(uuid = "u", publicKey = "p", shortId = "s", realityServerName = "example.com")
        val state = DeploymentUiState().reduce(DeploymentEvent.Completed(credentials))

        assertEquals(DeploymentPhase.Succeeded(credentials), state.phase)
    }

    @Test
    fun `a failed event sets the failure phase without touching steps`() {
        val state = DeploymentUiState().reduce(DeploymentEvent.Failed(IllegalStateException("no connection")))

        assertTrue(state.phase is DeploymentPhase.Failed)
        assertTrue(state.steps.all { it.status == StepStatus.Pending })
    }
}
