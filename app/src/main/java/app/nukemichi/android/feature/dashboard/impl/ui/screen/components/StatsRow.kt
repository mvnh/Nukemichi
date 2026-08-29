package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.vpn.XrayTrafficStats

@Composable
internal fun StatsRow(
    stats: XrayTrafficStats?,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.slimL))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(dimens.l),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatColumn(
            label = stringResource(R.string.dashboard_stats_down),
            value = formatBitrate(stats?.downlinkBytesPerSecond ?: 0L),
        )
        StatColumn(
            label = stringResource(R.string.dashboard_stats_up),
            value = formatBitrate(stats?.uplinkBytesPerSecond ?: 0L),
        )
        StatColumn(
            label = stringResource(R.string.dashboard_stats_total),
            value = formatBytes((stats?.downlinkTotalBytes ?: 0L) + (stats?.uplinkTotalBytes ?: 0L)),
        )
    }
}

// Plain String, not UiText: file-local literal labels + formatted numbers, no reuse.
@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatBitrate(bytesPerSecond: Long): String = "${formatBytes(bytesPerSecond)}/s"

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
