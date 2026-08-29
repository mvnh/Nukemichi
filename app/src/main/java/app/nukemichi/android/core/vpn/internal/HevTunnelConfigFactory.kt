package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.vpn.SocksEndpoint

internal object HevTunnelConfigFactory {

    private const val MAX_PORT = 65_535

    fun build(socksEndpoint: SocksEndpoint): String {
        require(socksEndpoint.host.isNotBlank()) { "Xray SOCKS host must not be blank." }
        require(socksEndpoint.port in 1..MAX_PORT) { "Invalid Xray SOCKS port: ${socksEndpoint.port}" }

        val lines = buildList {
            add("tunnel:")
            add("  name: nukemichi")
            add("  mtu: ${VpnTunnelDefaults.VPN_MTU}")
            add("  ipv4: ${VpnTunnelDefaults.VPN_ADDRESS}")
            add("socks5:")
            add("  address: ${socksEndpoint.host}")
            add("  port: ${socksEndpoint.port}")
            add("  udp: 'udp'")
            if (socksEndpoint.username != null && socksEndpoint.password != null) {
                add("  username: '${socksEndpoint.username}'")
                add("  password: '${socksEndpoint.password}'")
            }
            add("misc:")
            add("  log-level: warn")
        }
        return lines.joinToString("\n")
    }
}
