package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.feature.dashboard.impl.domain.model.ServerLibrary
import app.nukemichi.android.feature.dashboard.impl.ui.model.ServerDetailsUi
import app.nukemichi.android.feature.dashboard.impl.ui.model.ServerUi
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal fun ServerLibrary.toSubscriptionsUi(): ImmutableList<SubscriptionUi> {
    val selectedId = selectedServer?.id
    return subscriptions.map { subscription ->
        SubscriptionUi(
            id = subscription.id,
            name = subscription.name,
            isExpanded = subscription.id !in collapsedSubscriptionIds,
            containsSelectedServer = subscription.servers.any { it.id == selectedId },
            servers = subscription.servers.map { it.toServerUi(isSelected = it.id == selectedId) }.toImmutableList(),
        )
    }.toImmutableList()
}

internal fun XrayVpnProfile.toDetailsUi(): ServerDetailsUi {
    val reality = security as? XraySecurity.Reality
    return ServerDetailsUi(
        id = id,
        name = name,
        flag = countryCode?.toFlagEmoji(),
        stack = stackLabel(),
        address = "$serverAddress:$serverPort",
        maskingAs = reality?.serverName ?: (security as? XraySecurity.Tls)?.serverName,
        deployedAtMillis = deployedAtMillis,
        fingerprint = reality?.fingerprint,
        muxEnabled = muxEnabled,
        muxConcurrency = muxConcurrency,
    )
}

private fun XrayVpnProfile.toServerUi(isSelected: Boolean) = ServerUi(
    id = id,
    name = name,
    stack = stackLabel(),
    flag = countryCode?.toFlagEmoji(),
    isSelected = isSelected,
)
