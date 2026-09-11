package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.feature.dashboard.impl.ui.model.ServerDetailsUi
import app.nukemichi.android.platform.ui.components.ConfirmDialog
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens
import app.nukemichi.android.platform.ui.util.UiText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Everything about one server that is not the list row itself: what it is, sharing it, tuning it, forgetting it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServerDetailsSheet(
    details: ServerDetailsUi,
    isAdvancedMode: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onForget: () -> Unit,
    onFingerprintChanged: (XrayFingerprint) -> Unit,
    onMuxEnabledChanged: (Boolean) -> Unit,
    onMuxConcurrencyChanged: (Int) -> Unit,
) {
    val dimens = MaterialTheme.dimens
    var showForgetConfirm by rememberSaveable(details.id) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = dimens.l, end = dimens.l, bottom = dimens.l),
            verticalArrangement = Arrangement.spacedBy(dimens.l),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.l),
            ) {
                ServerFlag(flag = details.flag)
                Column {
                    Text(
                        text = details.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = details.stack,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(dimens.s)) {
                InfoRow(label = stringResource(R.string.dashboard_server_info_server), value = details.address)
                details.maskingAs?.let { maskingAs ->
                    InfoRow(label = stringResource(R.string.dashboard_server_info_masking_as), value = maskingAs)
                }
                InfoRow(
                    label = stringResource(R.string.dashboard_server_info_deployed),
                    value = formatDate(details.deployedAtMillis),
                )
            }

            FilledTonalButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(
                    imageVector = NukemichiIcons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.dashboard_export_link_label))
            }

            if (isAdvancedMode) {
                HorizontalDivider()
                ServerAdvancedSection(
                    fingerprint = details.fingerprint,
                    muxEnabled = details.muxEnabled,
                    muxConcurrency = details.muxConcurrency,
                    onFingerprintChanged = onFingerprintChanged,
                    onMuxEnabledChanged = onMuxEnabledChanged,
                    onMuxConcurrencyChanged = onMuxConcurrencyChanged,
                )
            }

            TextButton(
                onClick = { showForgetConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(text = stringResource(R.string.dashboard_server_forget))
            }
        }
    }

    if (showForgetConfirm) {
        ConfirmDialog(
            title = UiText.Resource(R.string.dashboard_server_forget_confirm_title),
            body = UiText.Resource(R.string.dashboard_server_forget_confirm_body),
            onConfirm = {
                showForgetConfirm = false
                onForget()
            },
            onDismiss = { showForgetConfirm = false },
        )
    }
}

// Plain String, not UiText: private helper always fed already-resolved stringResource(...)/
// formatted values inline, no reuse outside this file, no MVI-state boundary to cross.
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
