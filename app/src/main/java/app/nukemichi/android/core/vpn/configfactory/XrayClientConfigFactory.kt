package app.nukemichi.android.core.vpn.configfactory

import app.nukemichi.android.BuildConfig
import app.nukemichi.android.core.vpn.SocksEndpoint
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory.createRuntimeConfig
import app.nukemichi.android.core.vpn.spec.BurstObservatoryObject
import app.nukemichi.android.core.vpn.spec.DnsObject
import app.nukemichi.android.core.vpn.spec.FakeDnsObject
import app.nukemichi.android.core.vpn.spec.InboundObject
import app.nukemichi.android.core.vpn.spec.LogObject
import app.nukemichi.android.core.vpn.spec.MuxObject
import app.nukemichi.android.core.vpn.spec.OutboundObject
import app.nukemichi.android.core.vpn.spec.PingConfigObject
import app.nukemichi.android.core.vpn.spec.PolicyObject
import app.nukemichi.android.core.vpn.spec.SniffingObject
import app.nukemichi.android.core.vpn.spec.SystemPolicyObject
import app.nukemichi.android.core.vpn.spec.VlessOutboundSettings
import app.nukemichi.android.core.vpn.spec.VlessVnext
import app.nukemichi.android.core.vpn.spec.VlessVnextUser
import app.nukemichi.android.core.vpn.spec.XrayConfig
import app.nukemichi.android.core.vpn.spec.XrayTransport
import app.nukemichi.android.core.vpn.spec.toJsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.security.SecureRandom
import java.util.Base64

object XrayClientConfigFactory {
    private const val SOCKS_IN_TAG = "socks-in"
    private const val PROXY_OUTBOUND_TAG = "proxy"
    private const val LOOPBACK = "127.0.0.1"
    private const val SOCKS_PORT = 10_808
    private const val FAKE_DNS_MARKER = "fakedns"
    private const val FAKE_DNS_POOL = "198.18.0.0/15"
    private const val FAKE_DNS_POOL_SIZE = 65_535
    private const val HEALTH_CHECK_URL = "http://gstatic.com/generate_204"
    private const val SOCKS_CREDENTIAL_BYTES = 18

    fun createRuntimeConfig(profile: XrayVpnProfile): XrayRuntimeConfig {
        // See SocksEndpoint for why this can't be "noauth".
        val socksCredential = generateSocksCredential()
        return XrayRuntimeConfig(
            rawJson = build(profile, socksCredential).toJson(),
            socksEndpoint = SocksEndpoint(LOOPBACK, SOCKS_PORT, socksCredential.user, socksCredential.pass),
        )
    }

    /** The same shape of config [createRuntimeConfig] would run, minus a real [SocksEndpoint] to
     *  hand a runtime — for previewing/testing the generated JSON without a caller needing to
     *  thread a throwaway SOCKS credential through as well. */
    fun build(profile: XrayVpnProfile): XrayConfig = build(profile, generateSocksCredential())

    private fun build(profile: XrayVpnProfile, socksCredential: SocksCredential): XrayConfig {
        val proxyOutbound = OutboundObject(
            protocol = "vless",
            tag = PROXY_OUTBOUND_TAG,
            settings = VlessOutboundSettings(
                vnext = listOf(
                    VlessVnext(
                        address = profile.serverAddress,
                        port = profile.serverPort,
                        users = listOf(
                            // Vision only applies to raw TCP — it silently breaks XHTTP despite the
                            // TCP handshake succeeding, so it's only set for Raw transport.
                            VlessVnextUser(
                                id = profile.uuid,
                                encryption = "none",
                                flow = (profile.transport as? XrayTransport.Raw)?.flow?.wireValue,
                            ),
                        ),
                    ),
                ),
            ).toJsonObject(),
            streamSettings = XrayTransportFactory.streamSettings(profile.transport, profile.security),
            mux = if (profile.muxEnabled) {
                MuxObject(enabled = true, concurrency = profile.muxConcurrency)
            } else {
                null
            },
        )

        return XrayConfig(
            log = LogObject(loglevel = if (BuildConfig.DEBUG) "debug" else "warning"),
            stats = JsonObject(emptyMap()),
            policy = PolicyObject(
                system = SystemPolicyObject(
                    statsInboundUplink = true,
                    statsInboundDownlink = true,
                    statsOutboundUplink = true,
                    statsOutboundDownlink = true,
                ),
            ),
            // FakeDNS answers locally so DNS never needs a UDP round trip through the tunnel;
            // sniffing on socks-in recovers the real domain from the fake IP.
            dns = DnsObject(servers = listOf(JsonPrimitive(FAKE_DNS_MARKER))),
            fakedns = listOf(FakeDnsObject(ipPool = FAKE_DNS_POOL, poolSize = FAKE_DNS_POOL_SIZE)),
            inbounds = listOf(socksInbound(socksCredential)),
            outbounds = listOf(
                proxyOutbound,
                OutboundObject(protocol = "freedom", tag = "direct"),
                OutboundObject(protocol = "blackhole", tag = "block"),
                OutboundObject(protocol = "dns", tag = "dns-out"),
            ),
            routing = XrayRoutingFactory.build(
                socksInboundTag = SOCKS_IN_TAG,
                proxyOutboundTag = PROXY_OUTBOUND_TAG,
            ),
            // Gives xray's own accounting a health signal for the stuck-dial failure mode
            // (XTLS/Xray-core#6590) — see XrayHealthWatchdog for the actual detection.
            burstObservatory = BurstObservatoryObject(
                subjectSelector = listOf(PROXY_OUTBOUND_TAG),
                pingConfig = PingConfigObject(
                    destination = HEALTH_CHECK_URL,
                    interval = "1m",
                    sampling = 4,
                    timeout = "3s",
                ),
            ),
        )
    }

    /** "password" auth, not "noauth" — see SocksEndpoint for why. */
    private fun socksInbound(credential: SocksCredential) = InboundObject(
        listen = LOOPBACK,
        port = intPrimitive(SOCKS_PORT),
        protocol = "socks",
        tag = SOCKS_IN_TAG,
        settings = JsonObject(
            mapOf(
                "auth" to JsonPrimitive("password"),
                "accounts" to JsonArray(
                    listOf(
                        JsonObject(
                            mapOf(
                                "user" to JsonPrimitive(credential.user),
                                "pass" to JsonPrimitive(credential.pass),
                            )
                        )
                    )
                ),
                "udp" to JsonPrimitive(true),
            )
        ),
        sniffing = SniffingObject(
            enabled = true,
            destOverride = listOf(FAKE_DNS_MARKER, "http", "tls"),
        ),
    )

    private fun generateSocksCredential(): SocksCredential {
        val bytes = ByteArray(SOCKS_CREDENTIAL_BYTES)
        SecureRandom().nextBytes(bytes)
        val secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return SocksCredential(user = "nukemichi", pass = secret)
    }

    private data class SocksCredential(val user: String, val pass: String)
}
