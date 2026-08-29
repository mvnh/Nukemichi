package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport
import kotlinx.serialization.Serializable

@Serializable
data class XrayVpnProfile(
    val name: String,
    val sshHost: String,
    val sshPort: Int,
    val sshUsername: String,
    val sshExpectedFingerprint: String? = null,
    val serverAddress: String,
    val serverPort: Int,
    val uuid: String,
    // No default: every real profile needs real REALITY key material from a deployment.
    val security: XraySecurity,
    val transport: XrayTransport = XrayTransport.Xhttp(),
    val binaryUrl: String = "latest",
    val deployedAtMillis: Long = System.currentTimeMillis(),
    val muxEnabled: Boolean = false,
    val muxConcurrency: Int = DEFAULT_MUX_CONCURRENCY,
) {
    companion object {
        const val DEFAULT_MUX_CONCURRENCY = 8
    }
}
