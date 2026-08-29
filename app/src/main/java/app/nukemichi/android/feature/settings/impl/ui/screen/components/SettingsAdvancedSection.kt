package app.nukemichi.android.feature.settings.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.feature.settings.impl.ui.mvi.SettingsContract

private const val MIN_MUX_CONCURRENCY = 1
private const val MAX_MUX_CONCURRENCY = 128

@Composable
internal fun SettingsAdvancedSection(
    state: SettingsContract.State,
    onRealityServerNameChanged: (String) -> Unit,
    onFingerprintChanged: (XrayFingerprint) -> Unit,
    onMuxEnabledChanged: (Boolean) -> Unit,
    onMuxConcurrencyChanged: (Int) -> Unit,
    onExportVlessLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimens.l)) {
        Text(
            text = stringResource(id = R.string.settings_advanced_section_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(dimens.s)) {
            OutlinedTextField(
                value = state.realityServerName,
                onValueChange = onRealityServerNameChanged,
                label = { Text(stringResource(id = R.string.settings_sni_override_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(id = R.string.settings_sni_override_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(dimens.s)) {
            Text(
                text = stringResource(id = R.string.settings_utls_fingerprint_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.s)) {
                XrayFingerprint.entries.forEach { fingerprint ->
                    FilterChip(
                        selected = state.fingerprint == fingerprint,
                        onClick = { onFingerprintChanged(fingerprint) },
                        label = { Text(fingerprint.wireValue) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.settings_mux_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.settings_mux_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = state.muxEnabled, onCheckedChange = onMuxEnabledChanged)
        }

        if (state.muxEnabled) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.m)) {
                StepperButton(
                    symbol = "−",
                    onClick = {
                        onMuxConcurrencyChanged((state.muxConcurrency - 1).coerceAtLeast(MIN_MUX_CONCURRENCY))
                    },
                )
                Text(
                    text = stringResource(id = R.string.settings_mux_concurrency, state.muxConcurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StepperButton(
                    symbol = "+",
                    onClick = {
                        onMuxConcurrencyChanged((state.muxConcurrency + 1).coerceAtMost(MAX_MUX_CONCURRENCY))
                    },
                )
            }
        }

        OutlinedButton(onClick = onExportVlessLinkClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(id = R.string.settings_export_vless_link))
        }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Text(text = symbol, style = MaterialTheme.typography.titleMedium)
    }
}
