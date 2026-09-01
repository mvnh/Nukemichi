package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.spec.XraySecurity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VlessUriTest {

    private fun profile(name: String = "My Server", realityServerName: String = "www.example.com") =
        XrayVpnProfile(
            name = name,
            sshHost = "203.0.113.1",
            sshPort = 22,
            sshUsername = "root",
            serverAddress = "203.0.113.1",
            serverPort = 443,
            uuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            security = XraySecurity.Reality(
                serverName = realityServerName,
                publicKey = "publicKeyValue",
                shortId = "shortIdValue",
            ),
            deployedAtMillis = 0L,
        )

    @Test
    fun `includes host, port, and uuid unencoded`() {
        val uri = profile().toVlessUri()

        assertTrue(uri.startsWith("vless://a1b2c3d4-e5f6-7890-abcd-ef1234567890@203.0.113.1:443?"))
    }

    @Test
    fun `carries reality parameters clients expect`() {
        val uri = profile().toVlessUri()

        assertTrue(uri.contains("security=reality"))
        assertTrue(uri.contains("sni=www.example.com"))
        assertTrue(uri.contains("pbk=publicKeyValue"))
        assertTrue(uri.contains("sid=shortIdValue"))
        assertTrue(uri.contains("type=xhttp"))
    }

    @Test
    fun `sets mode and host explicitly rather than relying on an importing client's own defaults`() {
        // Regression test: an omitted mode left it to each importing client's own guess at
        // xray-core's REALITY-aware auto-resolution, which is not guaranteed to match. A
        // "context deadline exceeded" against a live server traced back to exactly this gap.
        val uri = profile().toVlessUri()

        assertTrue(uri.contains("mode=stream-one"))
        assertTrue(uri.contains("host=www.example.com"))
    }

    @Test
    fun `percent-encodes the remark instead of using form-encoding plus signs`() {
        val uri = profile(name = "My Server").toVlessUri()

        assertTrue(uri.endsWith("#My%20Server"))
    }

    @Test
    fun `percent-encodes a query value containing reserved characters`() {
        val uri = profile(realityServerName = "a b&c").toVlessUri()

        assertEquals(false, uri.contains("sni=a b&c"))
        assertTrue(uri.contains("sni=a%20b%26c"))
    }
}
