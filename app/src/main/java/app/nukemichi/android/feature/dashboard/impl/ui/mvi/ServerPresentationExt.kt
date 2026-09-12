package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport

// Protocol names, not prose: identical in every locale.
internal fun XrayVpnProfile.stackLabel(): String = listOfNotNull(
    "VLESS",
    when (transport) {
        is XrayTransport.Xhttp -> "XHTTP"
        is XrayTransport.Raw -> "RAW"
    },
    (transport as? XrayTransport.Raw)?.flow?.let { "VISION" },
    when (security) {
        is XraySecurity.Reality -> "REALITY"
        is XraySecurity.Tls -> "TLS"
    },
).joinToString(separator = " · ")

internal fun String.toFlagEmoji(): String? {
    if (length != 2 || any { it !in 'A'..'Z' }) return null
    return buildString { this@toFlagEmoji.forEach { appendCodePoint(REGIONAL_INDICATOR_A + (it - 'A')) } }
}

private const val REGIONAL_INDICATOR_A = 0x1F1E6
