package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentStep
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentStepUi
import app.nukemichi.android.feature.wizard.impl.ui.mvi.StepStatus

@Composable
internal fun DeploymentStepRow(step: DeploymentStepUi, modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.m),
    ) {
        StepIndicator(status = step.status)
        Text(
            text = step.step.label(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (step.status == StepStatus.Pending) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun StepIndicator(status: StepStatus, modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens
    val indicatorSize = dimens.xl

    when (status) {
        StepStatus.Pending -> Box(
            modifier = modifier
                .size(indicatorSize)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
        )

        StepStatus.Running -> CircularProgressIndicator(
            modifier = modifier.size(indicatorSize),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = dimens.xs,
        )

        StepStatus.Success -> Box(
            modifier = modifier
                .size(indicatorSize)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NukemichiIcons.Common.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(dimens.l),
            )
        }

        StepStatus.Failed -> Box(
            modifier = modifier
                .size(indicatorSize)
                .background(color = MaterialTheme.colorScheme.error, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NukemichiIcons.Navigation.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(dimens.l),
            )
        }
    }
}

@Composable
private fun DeploymentStep.label(): String = stringResource(
    when (this) {
        DeploymentStep.INSTALL_RUNTIME -> R.string.wizard_deployment_step_install_runtime
        DeploymentStep.FIND_SNI -> R.string.wizard_deployment_step_find_sni
        DeploymentStep.GENERATE_SECRETS -> R.string.wizard_deployment_step_generate_secrets
        DeploymentStep.WRITE_CONFIGURATION -> R.string.wizard_deployment_step_write_configuration
        DeploymentStep.START_SERVICE -> R.string.wizard_deployment_step_start_service
    }
)
