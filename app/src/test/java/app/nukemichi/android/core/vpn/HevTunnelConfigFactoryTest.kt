package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.internal.HevTunnelConfigFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression coverage for a real shipped bug: username/password lines built with the wrong
 *  indentation landed outside the `socks5:` mapping, which hev's YAML parser silently read as
 *  no-auth — every real connection was then rejected with "no matching auth method". */
class HevTunnelConfigFactoryTest {

    @Test
    fun `credentials nest under socks5, not at top level`() {
        val endpoint = SocksEndpoint("127.0.0.1", 10_808, "nukemichi", "secret")

        val lines = HevTunnelConfigFactory.build(endpoint).lines()
        val socks5Index = lines.indexOf("socks5:")
        val usernameIndex = lines.indexOfFirst { it.trimStart().startsWith("username:") }
        val passwordIndex = lines.indexOfFirst { it.trimStart().startsWith("password:") }

        assertEquals("  username: 'nukemichi'", lines[usernameIndex])
        assertEquals("  password: 'secret'", lines[passwordIndex])
        assertTrue("username must come after socks5:", usernameIndex > socks5Index)
        assertTrue("password must come after socks5:", passwordIndex > socks5Index)
    }

    @Test
    fun `an embedded single quote in a credential is doubled, not left to close the scalar early`() {
        val endpoint = SocksEndpoint("127.0.0.1", 10_808, "nuke'michi", "sec'ret")

        val lines = HevTunnelConfigFactory.build(endpoint).lines()

        assertEquals("  username: 'nuke''michi'", lines.first { it.trimStart().startsWith("username:") })
        assertEquals("  password: 'sec''ret'", lines.first { it.trimStart().startsWith("password:") })
    }

    @Test
    fun `no credentials means no auth lines at all`() {
        val endpoint = SocksEndpoint("127.0.0.1", 10_808)

        val config = HevTunnelConfigFactory.build(endpoint)

        assertFalse(config.contains("username"))
        assertFalse(config.contains("password"))
    }
}
