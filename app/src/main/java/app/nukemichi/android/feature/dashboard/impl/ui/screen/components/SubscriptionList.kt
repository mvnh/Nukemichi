package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionUi
import app.nukemichi.android.platform.ui.theme.size.dimens
import kotlinx.collections.immutable.ImmutableList

private const val SUBSCRIPTION_CONTENT_TYPE = "subscription"

/**
 * One lazy item per subscription, servers included. Expanding then changes that item's real height, and the
 * list reflows everything below it frame by frame. Per-server items would instead pop in and out whole.
 */
internal fun LazyListScope.subscriptionItems(
    subscriptions: ImmutableList<SubscriptionUi>,
    onToggleExpanded: (subscriptionId: String) -> Unit,
    onAddServer: (subscriptionId: String) -> Unit,
    onShare: (subscriptionId: String) -> Unit,
    onEdit: (subscriptionId: String) -> Unit,
    onSelectServer: (serverId: String) -> Unit,
    onServerDetails: (serverId: String) -> Unit,
) {
    itemsIndexed(
        items = subscriptions,
        key = { _, subscription -> "subscription:${subscription.id}" },
        contentType = { _, _ -> SUBSCRIPTION_CONTENT_TYPE },
    ) { index, subscription ->
        SubscriptionGroup(
            subscription = subscription,
            onToggleExpanded = { onToggleExpanded(subscription.id) },
            onAddServer = { onAddServer(subscription.id) },
            onShare = { onShare(subscription.id) },
            onEdit = { onEdit(subscription.id) },
            onSelectServer = onSelectServer,
            onServerDetails = onServerDetails,
            modifier = Modifier.padding(top = if (index == 0) 0.dp else MaterialTheme.dimens.l),
        )
    }
}
