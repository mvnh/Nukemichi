package app.nukemichi.android.core.vpn

import kotlinx.serialization.Serializable

@Serializable
data class XrayRuntimeConfig(
    val rawJson: String,
    val socksEndpoint: SocksEndpoint,
    val statusIntervalMillis: Long = 1_000L,
    // Nullable only so a restart intent scheduled by an older build still decodes.
    val serverId: String? = null,
)
