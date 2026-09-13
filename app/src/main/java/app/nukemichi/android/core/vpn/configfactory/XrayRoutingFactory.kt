package app.nukemichi.android.core.vpn.configfactory

import app.nukemichi.android.core.vpn.spec.BalancerObject
import app.nukemichi.android.core.vpn.spec.RoutingObject
import app.nukemichi.android.core.vpn.spec.RuleObject
import app.nukemichi.android.core.vpn.spec.StrategyObject
import app.nukemichi.android.core.vpn.spec.StrategySettingsObject

internal object XrayRoutingFactory {
    private const val PROXY_BALANCER_TAG = "balancer_proxy"

    fun build(socksInboundTag: String, proxyOutboundTag: String): RoutingObject = RoutingObject(
        domainStrategy = "IPIfNonMatch",
        rules = listOf(
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                port = intPrimitive(53),
                network = "udp",
                outboundTag = "dns-out",
            ),
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                ip = listOf("::/0"),
                outboundTag = "block",
            ),
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                port = intPrimitive(443),
                network = "udp",
                outboundTag = "block",
            ),
            // Domestic services in these countries commonly geo-fence to local IPs or block known
            // VPN exit ranges, so proxying them breaks them rather than protecting anything.
            // GeoAssetInstaller stages geosite.dat/geoip.dat into filesDir before Xray starts;
            // tools/geoip-dat/README.md covers the country list.
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                domain = listOf(
                    "geosite:category-ru",
                    "geosite:cn",
                    "geosite:category-ir",
                    "geosite:category-tm",
                ),
                outboundTag = "direct",
            ),
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                ip = listOf("geoip:ru", "geoip:by", "geoip:ir", "geoip:cn", "geoip:tm"),
                outboundTag = "direct",
            ),
            RuleObject(
                type = "field",
                inboundTag = listOf(socksInboundTag),
                network = "tcp,udp",
                balancerTag = PROXY_BALANCER_TAG,
            ),
        ),
        balancers = listOf(
            BalancerObject(
                tag = PROXY_BALANCER_TAG,
                selector = listOf(proxyOutboundTag),
                strategy = StrategyObject(
                    type = "leastLoad",
                    settings = StrategySettingsObject(
                        expected = 1,
                        maxRTT = "2s",
                        tolerance = 0f,
                        baselines = listOf("100ms", "300ms", "600ms", "1200ms"),
                    ),
                ),
            ),
        ),
    )
}
