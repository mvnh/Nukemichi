package app.nukemichi.android.feature.dashboard.impl.domain.model

import app.nukemichi.android.core.vpn.XraySubscription
import app.nukemichi.android.core.vpn.XrayVpnProfile

/** The stored subscriptions with the user's list preferences already resolved against them. */
internal data class ServerLibrary(
    val subscriptions: List<XraySubscription>,
    val selectedServer: XrayVpnProfile?,
    val collapsedSubscriptionIds: Set<String>,
)
