package app.nukemichi.android.feature.wizard.impl.ui.screen.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.DeploymentStepRow
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.DeploymentSuccessCelebration
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.LiveTerminalView
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentPhase
import app.nukemichi.android.feature.wizard.impl.ui.mvi.DeploymentUiState

@Composable
internal fun DeploymentProgressPage(
    deployment: DeploymentUiState,
    onToggleTerminal: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = deployment.phase is DeploymentPhase.Succeeded,
        modifier = modifier,
        label = "deployment_phase_transition",
    ) { isSucceeded ->
        if (isSucceeded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DeploymentSuccessCelebration(onFinishClick = onFinish, modifier = Modifier.padding(horizontal = MaterialTheme.dimens.l))
            }
        } else {
            DeploymentInProgressContent(
                deployment = deployment,
                onToggleTerminal = onToggleTerminal,
                onRetry = onRetry,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun DeploymentInProgressContent(
    deployment: DeploymentUiState,
    onToggleTerminal: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val dimens = MaterialTheme.dimens

    var lastFailure by remember { mutableStateOf<DeploymentPhase.Failed?>(null) }
    LaunchedEffect(deployment.phase) {
        (deployment.phase as? DeploymentPhase.Failed)?.let { lastFailure = it }
    }

    // Not WizardPage here — the terminal below needs a bounded-height parent to actually fill
    // the remaining space with Modifier.weight(1f) instead of a fixed cap; WizardPage's own
    // vertically-scrolling Column has unbounded height, which weight() can't work inside.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(dimens.l),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(dimens.l)) {
            deployment.steps.forEach { step -> DeploymentStepRow(step = step) }
        }

        AnimatedVisibility(
            visible = deployment.phase is DeploymentPhase.Failed,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            DeploymentErrorBanner(
                reason = lastFailure?.reason ?: UiText.Empty,
                onRetry = onRetry,
                onBack = onBack,
            )
        }

        Spacer(modifier = Modifier.height(dimens.l))

        LiveTerminalView(
            logLines = deployment.logLines,
            isExpanded = deployment.isTerminalExpanded,
            onToggleExpanded = onToggleTerminal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DeploymentErrorBanner(
    reason: UiText,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(dimens.l))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            Column(modifier = Modifier.padding(dimens.l)) {
                Text(
                    text = stringResource(R.string.wizard_deployment_error_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = Modifier.height(dimens.s))
                Text(text = reason.asString(), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(dimens.l))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.m),
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.wizard_deployment_back))
            }
            Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.wizard_deployment_retry))
            }
        }
    }
}
