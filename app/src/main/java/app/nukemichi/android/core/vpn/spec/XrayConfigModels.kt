package app.nukemichi.android.core.vpn.spec

import app.nukemichi.android.core.vpn.XrayJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Xray-core Configuration Data Classes
 * Based on the official Xray-core documentation.
 *
 * Fields whose JSON shape varies by protocol/context (e.g. a port that may be an
 * int or a range string, or a protocol's `settings` block) use [JsonElement] /
 * [JsonObject] / [JsonPrimitive] instead of `Any`, since kotlinx.serialization has
 * first-class support for those and `Any` cannot be serialized at all.
 */

@Serializable
data class XrayConfig(
    val env: Map<String, String>? = null,
    val log: LogObject? = null,
    val api: ApiObject? = null,
    val dns: DnsObject? = null,
    val routing: RoutingObject? = null,
    val policy: PolicyObject? = null,
    val inbounds: List<InboundObject>? = null,
    val outbounds: List<OutboundObject>? = null,
    val stats: JsonObject? = null,
    val fakedns: List<FakeDnsObject>? = null,
    val metrics: MetricsObject? = null,
    val observatory: ObservatoryObject? = null,
    val burstObservatory: BurstObservatoryObject? = null,
    val geodata: GeodataObject? = null,
    val version: VersionObject? = null
) {
    fun toJson(): String = XrayJson.default.encodeToString(serializer(), this)
}

// --- Base Modules ---

@Serializable
data class LogObject(
    val access: String? = null,
    val error: String? = null,
    val loglevel: String? = null, // "debug" | "info" | "warning" | "error" | "none"
    val dnsLog: Boolean? = null,
    val maskAddress: String? = null // "quarter" | "half" | "full"
)

@Serializable
data class ApiObject(
    val tag: String? = null,
    val listen: String? = null,
    val services: List<String>? = null
)

@Serializable
data class DnsObject(
    val hosts: Map<String, JsonElement>? = null, // Value can be a string or a list of strings
    val servers: List<JsonElement>? = null, // Can be a plain address string or a DnsServerObject
    val clientIp: String? = null,
    val queryStrategy: String? = null, // "UseIP" | "UseIPv4" | "UseIPv6" | "UseSystem"
    val disableCache: Boolean? = null,
    val serveStale: Boolean? = null,
    val serveExpiredTTL: Int? = null,
    val disableFallback: Boolean? = null,
    val disableFallbackIfMatch: Boolean? = null,
    val enableParallelQuery: Boolean? = null,
    val useSystemHosts: Boolean? = null,
    val tag: String? = null
)

@Serializable
data class DnsServerObject(
    val address: String? = null,
    val port: Int? = null,
    val domains: List<String>? = null,
    val expectedIPs: List<String>? = null,
    val unexpectedIPs: List<String>? = null,
    val skipFallback: Boolean? = null,
    val finalQuery: Boolean? = null,
    val tag: String? = null,
    val clientIP: String? = null,
    val queryStrategy: String? = null,
    val disableCache: Boolean? = null
)

@Serializable
data class FakeDnsObject(
    val ipPool: String? = null,
    val poolSize: Int? = null
)

@Serializable
data class RoutingObject(
    val domainStrategy: String? = null, // "AsIs" | "IPIfNonMatch" | "IPOnDemand"
    val rules: List<RuleObject>? = null,
    val balancers: List<BalancerObject>? = null
)

@Serializable
data class RuleObject(
    val type: String,
    val domain: List<String>? = null,
    val ip: List<String>? = null,
    val port: JsonPrimitive? = null, // Int or range string, e.g. "0-1023"
    val sourcePort: JsonPrimitive? = null,
    val localPort: JsonPrimitive? = null,
    val network: String? = null, // "tcp" | "udp" | "tcp,udp"
    val sourceIP: List<String>? = null,
    val localIP: List<String>? = null,
    val user: List<String>? = null,
    val vlessRoute: JsonPrimitive? = null,
    val inboundTag: List<String>? = null,
    val protocol: List<String>? = null,
    val attrs: Map<String, String>? = null,
    val process: List<String>? = null,
    val outboundTag: String? = null,
    val balancerTag: String? = null,
    val ruleTag: String? = null,
    val webhook: WebhookObject? = null
)

@Serializable
data class WebhookObject(
    val url: String? = null,
    val deduplication: Int? = null,
    val headers: Map<String, String>? = null
)

@Serializable
data class BalancerObject(
    val tag: String? = null,
    val selector: List<String>? = null,
    val fallbackTag: String? = null,
    val strategy: StrategyObject? = null
)

@Serializable
data class StrategyObject(
    val type: String? = null, // "random" | "roundRobin" | "leastPing" | "leastLoad"
    val settings: StrategySettingsObject? = null
)

@Serializable
data class StrategySettingsObject(
    val expected: Int? = null,
    val maxRTT: String? = null,
    val tolerance: Float? = null,
    val baselines: List<String>? = null,
    val costs: List<CostObject>? = null
)

@Serializable
data class CostObject(
    val regexp: Boolean? = null,
    val match: String? = null,
    val value: Float? = null
)

@Serializable
data class PolicyObject(
    val levels: Map<String, LevelPolicyObject>? = null,
    val system: SystemPolicyObject? = null
)

@Serializable
data class LevelPolicyObject(
    val handshake: Int? = null,
    val connIdle: Int? = null,
    val uplinkOnly: Int? = null,
    val downlinkOnly: Int? = null,
    val statsUserUplink: Boolean? = null,
    val statsUserDownlink: Boolean? = null,
    val statsUserOnline: Boolean? = null,
    val bufferSize: Int? = null
)

@Serializable
data class SystemPolicyObject(
    val statsInboundUplink: Boolean? = null,
    val statsInboundDownlink: Boolean? = null,
    val statsOutboundUplink: Boolean? = null,
    val statsOutboundDownlink: Boolean? = null
)

@Serializable
data class MetricsObject(
    val tag: String? = null,
    val listen: String? = null
)

@Serializable
data class ObservatoryObject(
    val subjectSelector: List<String>? = null,
    val probeUrl: String? = null,
    val probeInterval: String? = null,
    val enableConcurrency: Boolean? = null
)

@Serializable
data class BurstObservatoryObject(
    val subjectSelector: List<String>? = null,
    val pingConfig: PingConfigObject? = null
)

@Serializable
data class PingConfigObject(
    val destination: String? = null,
    val connectivity: String? = null,
    val interval: String? = null,
    val sampling: Int? = null,
    val timeout: String? = null,
    val httpMethod: String? = null
)

@Serializable
data class GeodataObject(
    val cron: String? = null,
    val outbound: String? = null,
    val assets: List<AssetObject>? = null
)

@Serializable
data class AssetObject(
    val url: String? = null,
    val file: String? = null
)

@Serializable
data class VersionObject(
    val min: String? = null,
    val max: String? = null
)

// --- Inbounds & Outbounds ---

@Serializable
data class InboundObject(
    val listen: String? = null,
    val port: JsonPrimitive? = null, // Int or range string, e.g. "443-450"
    val protocol: String? = null,
    val settings: JsonObject? = null, // Protocol-specific inbound settings; see typed *InboundSettings helpers below
    val streamSettings: StreamSettingsObject? = null,
    val tag: String? = null,
    val sniffing: SniffingObject? = null
)

@Serializable
data class SniffingObject(
    val enabled: Boolean? = null,
    val destOverride: List<String>? = null,
    val metadataOnly: Boolean? = null,
    val domainsExcluded: List<String>? = null,
    val ipsExcluded: List<String>? = null,
    val routeOnly: Boolean? = null
)

@Serializable
data class OutboundObject(
    val sendThrough: String? = null,
    val protocol: String? = null,
    val settings: JsonObject? = null, // Protocol-specific outbound settings; see typed *OutboundSettings helpers below
    val tag: String? = null,
    val streamSettings: StreamSettingsObject? = null,
    val proxySettings: ProxySettingsObject? = null,
    val mux: MuxObject? = null,
    val targetStrategy: String? = null
)

@Serializable
data class ProxySettingsObject(
    val tag: String? = null,
    val transportLayer: Boolean? = null
)

@Serializable
data class MuxObject(
    val enabled: Boolean? = null,
    val concurrency: Int? = null,
    val xudpConcurrency: Int? = null,
    val xudpProxyUDP443: String? = null
)

// --- Transport Settings ---

@Serializable
data class StreamSettingsObject(
    val method: String? = null,
    val rawSettings: RawObject? = null,
    val xhttpSettings: JsonObject? = null,
    val kcpSettings: KcpObject? = null,
    val grpcSettings: GrpcObject? = null,
    val wsSettings: WebSocketObject? = null,
    val httpupgradeSettings: HttpUpgradeObject? = null,
    val hysteriaSettings: HysteriaObject? = null,
    val security: String? = null, // "none" | "reality" | "tls"
    val realitySettings: RealityObject? = null,
    val tlsSettings: TlsObject? = null,
    val finalmask: FinalMaskObject? = null,
    val sockopt: SockoptObject? = null
)

@Serializable
data class RawObject(
    val acceptProxyProtocol: Boolean? = null,
    val header: JsonObject? = null // NoneHeaderObject or HttpHeaderObject shape
)

@Serializable
data class KcpObject(
    val mtu: Int? = null,
    val tti: Int? = null,
    val uplinkCapacity: Int? = null,
    val downlinkCapacity: Int? = null,
    val congestion: Boolean? = null,
    val readBufferSize: Int? = null,
    val writeBufferSize: Int? = null
)

@Serializable
data class GrpcObject(
    val authority: String? = null,
    val serviceName: String? = null,
    val multiMode: Boolean? = null,
    val user_agent: String? = null,
    val idle_timeout: Int? = null,
    val health_check_timeout: Int? = null,
    val permit_without_stream: Boolean? = null,
    val initial_windows_size: Int? = null
)

@Serializable
data class WebSocketObject(
    val acceptProxyProtocol: Boolean? = null,
    val path: String? = null,
    val host: String? = null,
    val headers: Map<String, String>? = null,
    val heartbeatPeriod: Int? = null
)

@Serializable
data class HttpUpgradeObject(
    val acceptProxyProtocol: Boolean? = null,
    val path: String? = null,
    val host: String? = null,
    val headers: Map<String, String>? = null
)

@Serializable
data class HysteriaObject(
    val version: Int? = null,
    val auth: String? = null,
    val udpIdleTimeout: Int? = null,
    val masquerade: MasqObject? = null
)

@Serializable
data class MasqObject(
    val type: String? = null,
    val dir: String? = null,
    val url: String? = null,
    val rewriteHost: Boolean? = null,
    val insecure: Boolean? = null,
    val content: String? = null,
    val headers: Map<String, String>? = null,
    val statusCode: Int? = null
)

// --- Security Settings ---

@Serializable
data class RealityObject(
    val show: Boolean? = null,
    val target: String? = null,
    val xver: Int? = null,
    val serverNames: List<String>? = null,
    val privateKey: String? = null,
    val minClientVer: String? = null,
    val maxClientVer: String? = null,
    val maxTimeDiff: Int? = null,
    val shortIds: List<String>? = null,
    val mldsa65Seed: String? = null,
    val limitFallbackUpload: LimitFallbackObject? = null,
    val limitFallbackDownload: LimitFallbackObject? = null,
    val serverName: String? = null,
    val fingerprint: String? = null,
    val password: String? = null,
    val shortId: String? = null,
    val mldsa65Verify: String? = null,
    val spiderX: String? = null
)

@Serializable
data class LimitFallbackObject(
    val afterBytes: Int? = null,
    val bytesPerSec: Int? = null,
    val burstBytesPerSec: Int? = null
)

@Serializable
data class TlsObject(
    val serverName: String? = null,
    val verifyPeerCertByName: String? = null,
    val rejectUnknownSni: Boolean? = null,
    val allowInsecure: Boolean? = null,
    val alpn: List<String>? = null,
    val minVersion: String? = null,
    val maxVersion: String? = null,
    val cipherSuites: String? = null,
    val certificates: List<CertificateObject>? = null,
    val disableSystemRoot: Boolean? = null,
    val enableSessionResumption: Boolean? = null,
    val fingerprint: String? = null,
    val pinnedPeerCertSha256: String? = null,
    val curvePreferences: List<String>? = null,
    val masterKeyLog: String? = null,
    val echServerKeys: String? = null,
    val echConfigList: String? = null,
    val echSockopt: SockoptObject? = null
)

@Serializable
data class CertificateObject(
    val ocspStapling: Int? = null,
    val oneTimeLoading: Boolean? = null,
    val usage: String? = null,
    val buildChain: Boolean? = null,
    val certificateFile: String? = null,
    val keyFile: String? = null,
    val certificate: List<String>? = null,
    val key: List<String>? = null
)

// --- Additional & Network Settings ---

@Serializable
data class FinalMaskObject(
    val tcp: List<MaskObject>? = null,
    val udp: List<MaskObject>? = null,
    val quicParams: QuicParamsObject? = null
)

@Serializable
data class MaskObject(
    val type: String? = null,
    val settings: JsonObject? = null
)

@Serializable
data class QuicParamsObject(
    val congestion: String? = null,
    val bbrProfile: String? = null,
    val debug: Boolean? = null,
    val brutalUp: String? = null,
    val brutalDown: String? = null,
    val udpHop: UdpHopObject? = null,
    val initStreamReceiveWindow: Int? = null,
    val maxStreamReceiveWindow: Int? = null,
    val initConnectionReceiveWindow: Int? = null,
    val maxConnectionReceiveWindow: Int? = null,
    val maxIdleTimeout: Int? = null,
    val keepAlivePeriod: Int? = null,
    val disablePathMTUDiscovery: Boolean? = null,
    val maxIncomingStreams: Int? = null
)

@Serializable
data class UdpHopObject(
    val ports: String? = null,
    val interval: Int? = null
)

@Serializable
data class SockoptObject(
    val mark: Int? = null,
    val tcpMaxSeg: Int? = null,
    val tcpFastOpen: JsonPrimitive? = null, // Boolean or Int
    val tproxy: String? = null,
    val domainStrategy: String? = null,
    val happyEyeballs: HappyEyeballsObject? = null,
    val dialerProxy: String? = null,
    val acceptProxyProtocol: Boolean? = null,
    val trustedXForwardedFor: List<String>? = null,
    val tcpKeepAliveInterval: Int? = null,
    val tcpKeepAliveIdle: Int? = null,
    val tcpUserTimeout: Int? = null,
    val tcpcongestion: String? = null,
    @SerialName("interface") val interfaceName: String? = null,
    val V6Only: Boolean? = null,
    val tcpWindowClamp: Int? = null,
    val tcpMptcp: Boolean? = null,
    val addressPortStrategy: String? = null,
    val customSockopt: List<CustomSockoptObject>? = null
)

@Serializable
data class CustomSockoptObject(
    val system: String? = null,
    val network: String? = null,
    val type: String? = null,
    val level: String? = null,
    val opt: String? = null,
    val value: String? = null
)

@Serializable
data class HappyEyeballsObject(
    val tryDelayMs: Int? = null,
    val prioritizeIPv6: Boolean? = null,
    val interleave: Int? = null,
    val maxConcurrentTry: Int? = null
)

// --- Typed protocol settings (VLESS only for now) ---
//
// InboundObject.settings / OutboundObject.settings stay generic JsonObject since
// their shape depends on `protocol`. These typed classes give compile-time safety
// when building VLESS blocks; flatten with `toJsonObject()` before assigning.

@Serializable
data class VlessClient(
    val id: String,
    val flow: String? = null,
    val email: String? = null,
    val level: Int? = null
)

// `decryption`/`encryption` deliberately have no default: XrayJson's encodeDefaults=false
// would silently drop a defaulted value from the emitted JSON, but xray-core expects
// these keys present even when the value is "none".
@Serializable
data class VlessInboundSettings(
    val clients: List<VlessClient>,
    val decryption: String
)

@Serializable
data class VlessVnextUser(
    val id: String,
    val encryption: String,
    val flow: String? = null,
    val level: Int? = null
)

@Serializable
data class VlessVnext(
    val address: String,
    val port: Int,
    val users: List<VlessVnextUser>
)

@Serializable
data class VlessOutboundSettings(
    val vnext: List<VlessVnext>
)

fun VlessInboundSettings.toJsonObject(): JsonObject =
    XrayJson.default.encodeToJsonElement(VlessInboundSettings.serializer(), this).jsonObject

fun VlessOutboundSettings.toJsonObject(): JsonObject =
    XrayJson.default.encodeToJsonElement(VlessOutboundSettings.serializer(), this).jsonObject
