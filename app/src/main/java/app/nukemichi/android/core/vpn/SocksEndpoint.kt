package app.nukemichi.android.core.vpn

import kotlinx.serialization.Serializable

@Serializable
data class SocksEndpoint(
    val host: String,
    val port: Int,
    /** SOCKS5 credentials, or null for no-auth. Null only for tests/endpoints with nothing to
     *  protect: Android doesn't sandbox loopback per-app, so any real inbound needs auth. */
    val username: String? = null,
    val password: String? = null,
)
