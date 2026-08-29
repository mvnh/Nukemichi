package app.nukemichi.android.core.vpn.internal

internal object VpnTunnelDefaults {
    const val VPN_ADDRESS = "10.8.0.2"
    const val VPN_PREFIX_LENGTH = 24
    const val VPN_MTU = 1500

    const val VPN_ADDRESS_V6 = "fd00:1:fd00:1::2"
    const val VPN_PREFIX_LENGTH_V6 = 64
    const val VPN_ROUTE_V6 = "::"
}
