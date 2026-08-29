package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Read-only — this is the dashboard's "think" surface. Editing the server lives in Settings. */
@Composable
internal fun ServerInfoCard(
    state: DashboardContract.State,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.slimL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(dimens.l),
            verticalArrangement = Arrangement.spacedBy(dimens.s),
        ) {
            InfoRow(label = stringResource(R.string.dashboard_server_info_server), value = state.serverAddress.orEmpty())
            state.realityServerName?.let {
                InfoRow(label = stringResource(R.string.dashboard_server_info_masking_as), value = it)
            }
            state.deployedAtMillis?.let {
                InfoRow(label = stringResource(R.string.dashboard_server_info_deployed), value = formatDate(it))
            }
        }
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
