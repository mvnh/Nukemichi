package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.platform.ui.theme.size.dimens

private const val MIN_MUX_CONCURRENCY = 1
private const val MAX_MUX_CONCURRENCY = 128

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ServerAdvancedSection(
    fingerprint: XrayFingerprint?,
    muxEnabled: Boolean,
    muxConcurrency: Int,
    onFingerprintChanged: (XrayFingerprint) -> Unit,
    onMuxEnabledChanged: (Boolean) -> Unit,
    onMuxConcurrencyChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimens.l)) {
        Text(
            text = stringResource(id = R.string.dashboard_server_advanced_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (fingerprint != null) {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.s)) {
                Text(
                    text = stringResource(id = R.string.dashboard_server_utls_fingerprint_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.s),
                    verticalArrangement = Arrangement.spacedBy(dimens.s),
                ) {
                    XrayFingerprint.entries.forEach { entry ->
                        FilterChip(
                            selected = fingerprint == entry,
                            onClick = { onFingerprintChanged(entry) },
                            label = { Text(entry.wireValue) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
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
                    text = stringResource(id = R.string.dashboard_server_mux_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.dashboard_server_mux_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = muxEnabled, onCheckedChange = onMuxEnabledChanged)
        }

        if (muxEnabled) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.m)) {
                StepperButton(
                    symbol = "−",
                    onClick = { onMuxConcurrencyChanged((muxConcurrency - 1).coerceAtLeast(MIN_MUX_CONCURRENCY)) },
                )
                Text(
                    text = stringResource(id = R.string.dashboard_server_mux_concurrency, muxConcurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StepperButton(
                    symbol = "+",
                    onClick = { onMuxConcurrencyChanged((muxConcurrency + 1).coerceAtMost(MAX_MUX_CONCURRENCY)) },
                )
            }
        }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Text(text = symbol, style = MaterialTheme.typography.titleMedium)
    }
}
