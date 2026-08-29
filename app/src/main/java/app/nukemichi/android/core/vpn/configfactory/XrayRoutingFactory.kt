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
