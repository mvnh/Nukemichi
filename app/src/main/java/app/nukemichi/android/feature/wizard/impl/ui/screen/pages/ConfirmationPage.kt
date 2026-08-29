package app.nukemichi.android.feature.wizard.impl.ui.screen.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.ServerAuthMethod
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.SetupStrategy
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.WizardPage

@Composable
fun ConfirmationPage(
    serverAddress: String,
    sshPort: String,
    username: String,
    authMethod: ServerAuthMethod,
    setupStrategy: SetupStrategy,
    hasAcknowledgedRisks: Boolean,
    onAcknowledgeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    WizardPage(modifier = modifier) {
        Text(
            text = stringResource(R.string.wizard_confirmation_summary_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(dimens.m))

        SummaryRow(
            label = stringResource(R.string.wizard_confirmation_server_label),
            value = "$serverAddress:$sshPort ($username)"
        )
        SummaryRow(
            label = stringResource(R.string.wizard_confirmation_auth_label),
            value = when (authMethod) {
                ServerAuthMethod.PASSWORD -> stringResource(R.string.wizard_server_data_password)
                ServerAuthMethod.SSH_KEY -> stringResource(R.string.wizard_server_data_ssh_key)
            }
        )
        SummaryRow(
            label = stringResource(R.string.wizard_confirmation_strategy_label),
            value = when (setupStrategy) {
                SetupStrategy.FAST_START -> stringResource(R.string.wizard_strategy_fast_start_title)
                SetupStrategy.NAIVEPROXY -> stringResource(R.string.wizard_strategy_resilience_title)
            }
        )

        Spacer(modifier = Modifier.height(dimens.l))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Column(modifier = Modifier.padding(dimens.l)) {
                Text(
                    text = stringResource(R.string.wizard_confirmation_warning_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(dimens.s))
                Text(
                    text = stringResource(R.string.wizard_confirmation_warning_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.l))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcknowledgeChange(!hasAcknowledgedRisks) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.s)
        ) {
            Checkbox(checked = hasAcknowledgedRisks, onCheckedChange = onAcknowledgeChange)
            Text(
                text = stringResource(R.string.wizard_confirmation_acknowledge),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimens.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
