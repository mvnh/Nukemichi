package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.core.vpn.spec.XraySecurity
import org.junit.Assert.assertTrue
import org.junit.Test

class XrayRuntimeConfigFactoryTest {
    @Test
    fun `creates a local SOCKS endpoint`() {
        val profile = XrayVpnProfile(
            name = "test",
            sshHost = "ssh.example",
            sshPort = 22,
            sshUsername = "root",
            serverAddress = "proxy.example",
            serverPort = 443,
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            security = XraySecurity.Reality(
                serverName = "www.example.com",
                publicKey = "public-key",
                shortId = "abcd",
            ),
            deployedAtMillis = 0L,
        )

        val config = XrayClientConfigFactory.createRuntimeConfig(profile)

        assertTrue(config.rawJson.contains("\"protocol\":\"vless\""))
        assertTrue(config.rawJson.contains("proxy.example"))
        assertTrue(config.rawJson.contains("\"method\":\"xhttp\""))
        assertTrue(config.rawJson.contains("\"password\":\"public-key\""))
        assertTrue(config.socksEndpoint.host == "127.0.0.1")
        assertTrue(config.socksEndpoint.port == 10_808)
        // Never noauth — see SocksEndpoint for why an unauthenticated loopback SOCKS proxy is a
        // real local-exploit surface (any other app on the device can reach 127.0.0.1).
        assertTrue(config.socksEndpoint.username != null)
        assertTrue(config.socksEndpoint.password != null)
        assertTrue(config.rawJson.contains("\"auth\":\"password\""))
        assertTrue(config.rawJson.contains(config.socksEndpoint.password!!))
    }
}
