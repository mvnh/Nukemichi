package app.nukemichi.android.core.vpn

import kotlinx.serialization.Serializable

/** A named group of servers. Deploying through the wizard either creates one or joins an existing one. */
@Serializable
data class XraySubscription(
    val id: String,
    val name: String,
    val servers: List<XrayVpnProfile>,
)
