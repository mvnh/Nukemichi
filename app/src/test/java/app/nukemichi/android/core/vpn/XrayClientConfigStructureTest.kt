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
 * Walks the parsed JSON rather than substring-matching it, because rule *order* decides the outcome
 * in Xray — first match wins — and a dropped rule degrades the tunnel instead of failing it.
 */
class XrayClientConfigStructureTest {

    @Test
    fun `xhttp transport never carries a flow, because vision breaks it after the handshake`() {
        val user = firstUser(config(transport = XrayTransport.Xhttp()))

        assertNull(user["flow"])
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

    /** Second line of defence behind the TUN exposing no IPv6 path at all. */
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

    /** Every app on the device can reach 127.0.0.1, and a constant credential would be no better than none. */
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
        deployedAtMillis = 0L,
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
