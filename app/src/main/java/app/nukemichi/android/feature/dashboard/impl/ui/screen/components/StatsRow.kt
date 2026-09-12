package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.style.TextOverflow
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.XrayTrafficStats
import app.nukemichi.android.platform.ui.theme.size.dimens

@Composable
internal fun StatsRow(
    stats: XrayTrafficStats?,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(dimens.l)) {
        StatCard(
            visible = visible,
            label = stringResource(R.string.dashboard_stats_down),
            value = formatBitrate(stats?.downlinkBytesPerSecond ?: 0L),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            visible = visible,
            label = stringResource(R.string.dashboard_stats_up),
            value = formatBitrate(stats?.uplinkBytesPerSecond ?: 0L),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            visible = visible,
            label = stringResource(R.string.dashboard_stats_total),
            value = formatBytes((stats?.downlinkTotalBytes ?: 0L) + (stats?.uplinkTotalBytes ?: 0L)),
            modifier = Modifier.weight(1f),
        )
    }
}

// Each card reveals behind its own rounded clip: one clip across the row would leave the middle card's
// corners sawn off square while the row grows.
// Plain String, not UiText: file-local literal labels + formatted numbers, no reuse.
@Composable
private fun StatCard(visible: Boolean, label: String, value: String, modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens
    val cardShape = RoundedCornerShape(dimens.cornerRadius)

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.clip(cardShape),
        enter = expandVertically(animationSpec = dashboardSpatialSpec(), expandFrom = Alignment.Top) +
            fadeIn(animationSpec = dashboardEffectsSpec()),
        exit = shrinkVertically(animationSpec = dashboardSpatialSpec(), shrinkTowards = Alignment.Top) +
            fadeOut(animationSpec = dashboardEffectsSpec()),
    ) {
        StatCardContent(label = label, value = value)
    }
}

@Composable
private fun StatCardContent(label: String, value: String) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = Modifier
            // Its own top spacing, so the gap above the cards appears and disappears with them.
            .padding(top = dimens.xl)
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = dimens.l, horizontal = dimens.m),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
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
