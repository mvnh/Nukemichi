package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.spec.XraySecurity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XraySubscriptionsExtTest {

    private fun server(id: String, address: String, uuid: String = "uuid-$id") = XrayVpnProfile(
        id = id,
        name = "Server $id",
        sshHost = address,
        sshPort = 22,
        sshUsername = "root",
        serverAddress = address,
        serverPort = 443,
        uuid = uuid,
        security = XraySecurity.Reality(serverName = "www.example.com", publicKey = "key", shortId = "ab"),
        deployedAtMillis = 0L,
    )

    private fun subscription(id: String, vararg servers: XrayVpnProfile) =
        XraySubscription(id = id, name = "Group $id", servers = servers.toList())

    private val unusedNewSubscription: () -> XraySubscription = { error("must not create a new subscription") }

    @Test
    fun `redeploying a known address replaces that entry in place, keeping its id, name and group`() {
        val subscriptions = listOf(
            subscription("home", server("a", "203.0.113.1")),
            subscription("work", server("b", "203.0.113.2")),
        )

        val redeployed = server("fresh-id", "203.0.113.1", uuid = "rotated-uuid").copy(name = "Fresh name")
        val result = subscriptions.withDeployedServer(redeployed, targetSubscriptionId = "work", unusedNewSubscription)

        assertEquals(listOf("a"), result.findSubscription("home")!!.servers.map { it.id })
        assertEquals(listOf("b"), result.findSubscription("work")!!.servers.map { it.id })
        val stored = result.findServer("a")!!
        assertEquals("Server a", stored.name)
        assertEquals("rotated-uuid", stored.uuid)
        assertNull(result.findServer("fresh-id"))
    }

    @Test
    fun `a new address joins the target subscription after its existing servers`() {
        val subscriptions = listOf(subscription("home", server("a", "203.0.113.1")))

        val result = subscriptions.withDeployedServer(server("b", "203.0.113.2"), "home", unusedNewSubscription)

        assertEquals(1, result.size)
        assertEquals(listOf("a", "b"), result.single().servers.map { it.id })
    }

    @Test
    fun `a new address lands in a new subscription when there is no target or it was deleted`() {
        val subscriptions = listOf(subscription("home", server("a", "203.0.113.1")))

        listOf(null, "deleted").forEach { target ->
            var created = false
            val result = subscriptions.withDeployedServer(server("b", "203.0.113.2"), target) {
                created = true
                XraySubscription(id = "new", name = "New", servers = emptyList())
            }

            assertTrue("target=$target must create a subscription", created)
            assertEquals(listOf("home", "new"), result.map { it.id })
            assertEquals(listOf("b"), result.findSubscription("new")!!.servers.map { it.id })
        }
    }

    @Test
    fun `removing a group's last server removes the group, other groups stay`() {
        val subscriptions = listOf(
            subscription("home", server("a", "203.0.113.1")),
            subscription("work", server("b", "203.0.113.2"), server("c", "203.0.113.3")),
        )

        val withoutA = subscriptions.withoutServer("a")
        assertEquals(listOf("work"), withoutA.map { it.id })

        val withoutB = subscriptions.withoutServer("b")
        assertEquals(listOf("home", "work"), withoutB.map { it.id })
        assertEquals(listOf("c"), withoutB.findSubscription("work")!!.servers.map { it.id })
    }

    @Test
    fun `updating a server touches only that server`() {
        val subscriptions = listOf(subscription("home", server("a", "203.0.113.1"), server("b", "203.0.113.2")))

        val result = subscriptions.withServerUpdated("b") { it.copy(muxEnabled = true) }

        assertFalse(result.findServer("a")!!.muxEnabled)
        assertTrue(result.findServer("b")!!.muxEnabled)
    }
}
