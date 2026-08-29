package app.nukemichi.android.core.vpn.spec

import kotlinx.serialization.Serializable

@Serializable
sealed interface XraySecurity {
    @Serializable
    data class Reality(
        val serverName: String,
        val publicKey: String,
        val shortId: String,
        val fingerprint: XrayFingerprint = XrayFingerprint.EDGE,
    ) : XraySecurity

    @Serializable
    data class Tls(
        val serverName: String,
        val allowInsecure: Boolean = false,
    ) : XraySecurity
}
