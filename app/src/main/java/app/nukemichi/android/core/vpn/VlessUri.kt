package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport
import java.net.URLEncoder

fun XrayVpnProfile.toVlessUri(): String = buildString {
    append("vless://")
    append(uuid)
    append('@')
    append(serverAddress)
    append(':')
    append(serverPort)
    append("?encryption=none")

    when (val security = security) {
        is XraySecurity.Reality -> {
            append("&security=reality")
            append("&sni=").append(security.serverName.urlEncode())
            append("&fp=").append(security.fingerprint.wireValue.urlEncode())
            append("&pbk=").append(security.publicKey.urlEncode())
            append("&sid=").append(security.shortId.urlEncode())
        }
        is XraySecurity.Tls -> {
            append("&security=tls")
            append("&sni=").append(security.serverName.urlEncode())
            if (security.allowInsecure) append("&allowInsecure=1")
        }
    }

    when (val transport = transport) {
        is XrayTransport.Xhttp -> {
            append("&type=xhttp&path=").append(transport.path.urlEncode())
            append("&mode=").append(transport.mode.wireValue.urlEncode())
            val host = (security as? XraySecurity.Reality)?.serverName
                ?: (security as? XraySecurity.Tls)?.serverName
            host?.let { append("&host=").append(it.urlEncode()) }
        }
        is XrayTransport.Raw -> {
            append("&type=raw")
            transport.flow?.let { flow -> append("&flow=").append(flow.wireValue.urlEncode()) }
        }
    }

    append('#').append(name.urlEncode())
}

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
