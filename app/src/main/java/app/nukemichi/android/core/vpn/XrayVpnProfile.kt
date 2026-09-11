package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport
import kotlinx.serialization.Serializable

@Serializable
data class XrayVpnProfile(
    // Stable across edits and redeploys: subscriptions, selection and list keys all address a server by it.
    val id: String,
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
    val deployedAtMillis: Long,
    // ISO 3166-1 alpha-2 code from a GeoIP lookup the VPS made about itself; null when it could not tell.
    val countryCode: String? = null,
    val muxEnabled: Boolean = false,
    val muxConcurrency: Int = DEFAULT_MUX_CONCURRENCY,
) {
    companion object {
        const val DEFAULT_MUX_CONCURRENCY = 8
    }
}
