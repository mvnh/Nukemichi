package app.nukemichi.android.core.vpn

import kotlinx.serialization.Serializable

@Serializable
data class XraySubscription(
    val id: String,
    val name: String,
    val servers: List<XrayVpnProfile>,
)
