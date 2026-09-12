package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.configfactory.XrayServerConfigFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class XrayServerConfigFactoryTest {
    @Test
    fun `creates a REALITY server config with resolved secrets`() {
        val config = XrayServerConfigFactory.build(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            privateKey = "server-private-key",
            shortId = "abcd1234",
            realityServerName = "www.cloudflare.com"
        ).toJson()

        assertTrue(config.contains("\"method\":\"xhttp\""))
        assertTrue(config.contains("\"target\":\"www.cloudflare.com:443\""))
        assertTrue(config.contains("\"privateKey\":\"server-private-key\""))
        assertTrue(config.contains("\"id\":\"123e4567-e89b-12d3-a456-426614174000\""))
        assertTrue(config.contains("\"shortIds\":[\"abcd1234\"]"))
        assertTrue(config.contains("\"decryption\":\"none\""))
    }

    /**
     * REALITY's target is the site being impersonated, and that site listens on 443 whatever port
     * the inbound happens to use. Deriving it from serverPort pointed the handshake at a port the
     * masking host has nothing on, which fails only once traffic is actually flowing.
     */
    @Test
    fun `the masking target stays on 443 when the inbound listens elsewhere`() {
        val config = XrayServerConfigFactory.build(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            privateKey = "server-private-key",
            shortId = "abcd1234",
            serverPort = 8443,
            realityServerName = "www.cloudflare.com",
        ).toJson()

        assertTrue("inbound must honour the requested port", config.contains("\"port\":8443"))
        assertTrue(
            "REALITY dest must stay on the masking site's own port: $config",
            config.contains("\"target\":\"www.cloudflare.com:443\""),
        )
    }
}
