package app.nukemichi.android.core.vpn.configfactory

import app.nukemichi.android.core.vpn.spec.InboundObject
import app.nukemichi.android.core.vpn.spec.LogObject
import app.nukemichi.android.core.vpn.spec.OutboundObject
import app.nukemichi.android.core.vpn.spec.RealityObject
import app.nukemichi.android.core.vpn.spec.VlessClient
import app.nukemichi.android.core.vpn.spec.VlessInboundSettings
import app.nukemichi.android.core.vpn.spec.XrayConfig
import app.nukemichi.android.core.vpn.spec.toJsonObject

object XrayServerConfigFactory {
    const val DEFAULT_SERVER_PORT = 443

    fun build(
        uuid: String,
        privateKey: String,
        shortId: String,
        serverPort: Int = DEFAULT_SERVER_PORT,
        realityServerName: String
    ): XrayConfig = XrayConfig(
        log = LogObject(loglevel = "warning"),
        inbounds = listOf(
            InboundObject(
                listen = "0.0.0.0",
                port = intPrimitive(serverPort),
                protocol = "vless",
                settings = VlessInboundSettings(
                    clients = listOf(VlessClient(id = uuid)),
                    decryption = "none",
                ).toJsonObject(),
                streamSettings = XrayTransportFactory.serverXhttpRealityStreamSettings(
                    RealityObject(
                        show = false,
                        target = "$realityServerName:$serverPort",
                        xver = 0,
                        serverNames = listOf(realityServerName),
                        privateKey = privateKey,
                        shortIds = listOf(shortId),
                    ),
                ),
            ),
        ),
        outbounds = listOf(OutboundObject(protocol = "freedom", tag = "direct")),
    )
}
