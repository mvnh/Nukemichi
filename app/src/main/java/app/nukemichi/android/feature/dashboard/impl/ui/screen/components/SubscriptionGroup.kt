package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionUi
import app.nukemichi.android.platform.ui.theme.size.dimens

@Composable
internal fun SubscriptionGroup(
    subscription: SubscriptionUi,
    onToggleExpanded: () -> Unit,
    onAddServer: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onSelectServer: (serverId: String) -> Unit,
    onServerDetails: (serverId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier) {
        SubscriptionHeaderRow(
            subscription = subscription,
            onToggleExpanded = onToggleExpanded,
            onAddServer = onAddServer,
            onShare = onShare,
            onEdit = onEdit,
        )

        AnimatedVisibility(
            visible = subscription.isExpanded,
            enter = expandVertically(animationSpec = dashboardSpatialSpec(), expandFrom = Alignment.Top) +
                fadeIn(animationSpec = dashboardEffectsSpec()),
            exit = shrinkVertically(animationSpec = dashboardSpatialSpec(), shrinkTowards = Alignment.Top) +
                fadeOut(animationSpec = dashboardEffectsSpec()),
        ) {
            val segmentCount = subscription.servers.size + 1
            Column(
                modifier = Modifier
                    .animateContentSize(animationSpec = dashboardSpatialSpec())
                    .padding(top = dimens.xs),
                verticalArrangement = Arrangement.spacedBy(dimens.xs),
            ) {
                subscription.servers.forEachIndexed { index, server ->
                    key(server.id) {
                        ServerRow(
                            server = server,
                            shape = segmentShape(index = index + 1, count = segmentCount),
                            onClick = { onSelectServer(server.id) },
                            onDetailsClick = { onServerDetails(server.id) },
                        )
                    }
                }
            }
        }
    }
}
