package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionUi
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens

private const val EXPANDED_CHEVRON_ROTATION = 180f

@Composable
internal fun SubscriptionHeaderRow(
    subscription: SubscriptionUi,
    onToggleExpanded: () -> Unit,
    onAddServer: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    val colors = MaterialTheme.colorScheme
    val isHighlighted = !subscription.isExpanded && subscription.containsSelectedServer

    // Everything that differs between the expanded and collapsed header eases across on the same
    // clock as the servers folding beneath it, instead of snapping ahead of the press ripple.
    val containerColor by animateColorAsState(
        targetValue = if (isHighlighted) colors.primaryContainer else colors.surfaceContainerLow,
        animationSpec = dashboardEffectsSpec(),
        label = "subscriptionHeaderContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isHighlighted) colors.onPrimaryContainer else colors.onSurface,
        animationSpec = dashboardEffectsSpec(),
        label = "subscriptionHeaderContent",
    )
    val bottomCorner by animateDpAsState(
        targetValue = if (subscription.isExpanded) SegmentInnerCorner else SegmentOuterCorner,
        animationSpec = dashboardSpatialSpec(),
        label = "subscriptionHeaderBottomCorner",
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (subscription.isExpanded) EXPANDED_CHEVRON_ROTATION else 0f,
        animationSpec = dashboardSpatialSpec(),
        label = "subscriptionHeaderChevron",
    )

    Surface(
        onClick = onToggleExpanded,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = SegmentOuterCorner,
            topEnd = SegmentOuterCorner,
            bottomEnd = bottomCorner,
            bottomStart = bottomCorner,
        ),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = dimens.xxl + dimens.m)
                .padding(start = dimens.l, end = dimens.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.l),
        ) {
            // Same slot width as a server's flag, so group and server names line up.
            Box(modifier = Modifier.size(dimens.control), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = NukemichiIcons.Outlined.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.dashboard_subscription_own_servers, subscription.servers.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Row {
                IconButton(onClick = onAddServer) {
                    Icon(
                        imageVector = NukemichiIcons.Outlined.Add,
                        contentDescription = stringResource(R.string.dashboard_subscription_add_server_cd, subscription.name),
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = NukemichiIcons.Outlined.Share,
                        contentDescription = stringResource(R.string.dashboard_subscription_share_cd, subscription.name),
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = NukemichiIcons.Outlined.Edit,
                        contentDescription = stringResource(R.string.dashboard_subscription_edit_cd, subscription.name),
                    )
                }
            }
        }
    }
}
