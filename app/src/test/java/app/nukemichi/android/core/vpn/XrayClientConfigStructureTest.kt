package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.core.vpn.spec.XrayFlow
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The generated config is what the tunnel actually runs on, and a regression in it does not throw —
 * it degrades. A dropped routing rule leaks traffic that was supposed to be blackholed; a stray
 * `flow` on the wrong transport makes the tunnel connect and then quietly fail every request. Both
 * look like "the VPN is a bit broken today" rather than like a bug with a cause.
 *
 * These assertions walk the parsed JSON rather than substring-matching it, so that rule *order* —
 * which decides the outcome in Xray, first match wins — is covered too.
 */
class XrayClientConfigStructureTest {

    /**
     * The comment in XrayClientConfigFactory says Vision "silently breaks XHTTP despite the TCP
     * handshake succeeding". That is a debugging session someone already paid for once; this makes
     * sure nobody pays for it twice.
     */
    @Test
    fun `xhttp transport never carries a flow`() {
        val user = firstUser(config(transport = XrayTransport.Xhttp()))

        assertNull("Vision on XHTTP connects and then fails every request", user["flow"])
    }

    @Test
    fun `raw transport carries the flow it was configured with`() {
        val user = firstUser(config(transport = XrayTransport.Raw(flow = XrayFlow.VISION)))

        assertEquals("xtls-rprx-vision", user.getValue("flow").jsonPrimitive.content)
    }

    @Test
    fun `raw transport without a flow omits the field entirely`() {
        assertNull(firstUser(config(transport = XrayTransport.Raw()))["flow"])
    }

    /**
     * Rule order is the routing policy. Anything landing after the catch-all is dead, and the
     * blackholes only work if they are matched before it.
     */
    @Test
    fun `routing sends DNS out, blackholes IPv6 and QUIC, then proxies the rest`() {
        val rules = config().routing().getValue("rules").jsonArray.map { it.jsonObject }

        assertEquals(4, rules.size)

        assertEquals("dns-out", rules[0].outboundTag())
        assertEquals("udp", rules[0].getValue("network").jsonPrimitive.content)
        assertEquals(53, rules[0].getValue("port").jsonPrimitive.content.toInt())

        assertEquals("block", rules[1].outboundTag())
        assertEquals(listOf("::/0"), rules[1].getValue("ip").jsonArray.map { it.jsonPrimitive.content })

        assertEquals("block", rules[2].outboundTag())
        assertEquals(443, rules[2].getValue("port").jsonPrimitive.content.toInt())
        assertEquals("udp", rules[2].getValue("network").jsonPrimitive.content)

        assertNull("the catch-all proxies via the balancer, not a direct outbound", rules[3]["outboundTag"])
        assertEquals("balancer_proxy", rules[3].getValue("balancerTag").jsonPrimitive.content)
        assertEquals("tcp,udp", rules[3].getValue("network").jsonPrimitive.content)
    }

    /**
     * The TUN interface deliberately exposes no IPv6 path, so this rule is the second line of
     * defence: anything that still manages to dial v6 through the SOCKS inbound gets blackholed
     * rather than handed to the proxy.
     */
    @Test
    fun `the IPv6 blackhole is matched before the catch-all`() {
        val rules = config().routing().getValue("rules").jsonArray.map { it.jsonObject }

        val blackholeIndex = rules.indexOfFirst { it["ip"]?.jsonArray?.any { ip -> ip.jsonPrimitive.content == "::/0" } == true }
        val catchAllIndex = rules.indexOfFirst { it["balancerTag"] != null }

        // Checked before the ordering comparison: indexOfFirst returns -1 when absent, and -1 is
        // "before" everything — so ordering alone would silently pass on a deleted rule.
        assertTrue("the IPv6 blackhole rule is gone entirely", blackholeIndex >= 0)
        assertTrue("the catch-all rule is gone entirely", catchAllIndex >= 0)
        assertTrue("IPv6 blackhole is unreachable after the catch-all", blackholeIndex < catchAllIndex)
    }

    @Test
    fun `every outbound the routing refers to actually exists`() {
        val config = config()
        val declared = config.getValue("outbounds").jsonArray.map { it.jsonObject.getValue("tag").jsonPrimitive.content }
        val referenced = config.routing().getValue("rules").jsonArray
            .mapNotNull { it.jsonObject.outboundTag() }

        assertTrue("routing points at outbounds that do not exist: ${referenced - declared.toSet()}", declared.containsAll(referenced))
    }

    @Test
    fun `the balancer selects the proxy outbound`() {
        val balancer = config().routing().getValue("balancers").jsonArray.single().jsonObject

        assertEquals("balancer_proxy", balancer.getValue("tag").jsonPrimitive.content)
        assertEquals(
            listOf("proxy"),
            balancer.getValue("selector").jsonArray.map { it.jsonPrimitive.content },
        )
    }

    @Test
    fun `reality settings carry the public key and the masking server name`() {
        val stream = config().outbound("proxy").getValue("streamSettings").jsonObject
        val reality = stream.getValue("realitySettings").jsonObject

        assertEquals("xhttp", stream.getValue("method").jsonPrimitive.content)
        assertEquals("reality", stream.getValue("security").jsonPrimitive.content)
        assertEquals("public-key", reality.getValue("password").jsonPrimitive.content)
        assertEquals("www.example.com", reality.getValue("serverName").jsonPrimitive.content)
        assertNull("a REALITY outbound must not also carry TLS settings", stream["tlsSettings"])
    }

    /**
     * The loopback SOCKS inbound is reachable by every other app on the device, so the credential
     * is what keeps it from being an open local proxy. It has to be present, and it has to differ
     * per session — a constant would be no better than none.
     */
    @Test
    fun `the socks inbound requires a freshly generated credential`() {
        val first = XrayClientConfigFactory.createRuntimeConfig(profile())
        val second = XrayClientConfigFactory.createRuntimeConfig(profile())

        assertNotEquals(first.socksEndpoint.password, second.socksEndpoint.password)

        val account = Json.parseToJsonElement(first.rawJson).jsonObject
            .getValue("inbounds").jsonArray.single().jsonObject
            .getValue("settings").jsonObject
            .getValue("accounts").jsonArray.single().jsonObject

        assertEquals(first.socksEndpoint.username, account.getValue("user").jsonPrimitive.content)
        assertEquals(first.socksEndpoint.password, account.getValue("pass").jsonPrimitive.content)
        assertFalse("a generated credential must not be empty", first.socksEndpoint.password.isNullOrEmpty())
    }

    @Test
    fun `sniffing recovers the real destination from the fakedns address`() {
        val sniffing = config().getValue("inbounds").jsonArray.single().jsonObject
            .getValue("sniffing").jsonObject

        assertTrue(sniffing.getValue("enabled").jsonPrimitive.content.toBoolean())
        assertEquals(
            listOf("fakedns", "http", "tls"),
            sniffing.getValue("destOverride").jsonArray.map { it.jsonPrimitive.content },
        )
    }

    private fun profile(transport: XrayTransport = XrayTransport.Xhttp()) = XrayVpnProfile(
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
        transport = transport,
    )

    private fun config(transport: XrayTransport = XrayTransport.Xhttp()): JsonObject =
        Json.parseToJsonElement(XrayClientConfigFactory.build(profile(transport)).toJson()).jsonObject

    private fun JsonObject.routing(): JsonObject = getValue("routing").jsonObject

    private fun JsonObject.outbound(tag: String): JsonObject =
        getValue("outbounds").jsonArray.map { it.jsonObject }
            .single { it.getValue("tag").jsonPrimitive.content == tag }

    private fun JsonObject.outboundTag(): String? = this["outboundTag"]?.jsonPrimitive?.content

    private fun firstUser(config: JsonObject): JsonObject =
        config.outbound("proxy")
            .getValue("settings").jsonObject
            .getValue("vnext").jsonArray.single().jsonObject
            .getValue("users").jsonArray.single().jsonObject
}
