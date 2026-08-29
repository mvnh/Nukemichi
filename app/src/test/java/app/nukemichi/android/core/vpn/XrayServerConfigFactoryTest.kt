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
}
