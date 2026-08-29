package app.nukemichi.android.core.vpn.configfactory

import app.nukemichi.android.core.vpn.spec.RawObject
import app.nukemichi.android.core.vpn.spec.RealityObject
import app.nukemichi.android.core.vpn.spec.StreamSettingsObject
import app.nukemichi.android.core.vpn.spec.TlsObject
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport

object XrayTransportFactory {
    const val DEFAULT_XHTTP_PATH = "/"

    fun streamSettings(transport: XrayTransport, security: XraySecurity): StreamSettingsObject {
        val (method, xhttpSettings, rawSettings) = when (transport) {
            is XrayTransport.Xhttp -> Triple(
                "xhttp",
                jsonObjectOf("path" to transport.path, "mode" to transport.mode.wireValue),
                null,
            )
            is XrayTransport.Raw -> Triple("raw", null, RawObject())
        }
        val (securityName, realitySettings, tlsSettings) = when (security) {
            is XraySecurity.Reality -> Triple(
                "reality",
                RealityObject(
                    serverName = security.serverName,
                    fingerprint = security.fingerprint.wireValue,
                    password = security.publicKey,
                    shortId = security.shortId,
                ),
                null,
            )
            is XraySecurity.Tls -> Triple(
                "tls",
                null,
                TlsObject(serverName = security.serverName, allowInsecure = security.allowInsecure),
            )
        }
        return StreamSettingsObject(
            method = method,
            security = securityName,
            xhttpSettings = xhttpSettings,
            rawSettings = rawSettings,
            realitySettings = realitySettings,
            tlsSettings = tlsSettings,
        )
    }

    fun serverXhttpRealityStreamSettings(reality: RealityObject): StreamSettingsObject =
        StreamSettingsObject(
            method = "xhttp",
            security = "reality",
            xhttpSettings = jsonObjectOf("path" to DEFAULT_XHTTP_PATH),
            realitySettings = reality,
        )
}
