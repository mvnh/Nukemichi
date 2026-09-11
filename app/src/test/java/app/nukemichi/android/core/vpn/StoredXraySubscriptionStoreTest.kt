package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.core.vpn.internal.StoredXraySubscriptionStore
import app.nukemichi.android.core.vpn.spec.XraySecurity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoredXraySubscriptionStoreTest {

    private val legacyProfile = XrayVpnProfile(
        id = "ignored",
        name = "Silent Harbor",
        sshHost = "203.0.113.9",
        sshPort = 22,
        sshUsername = "root",
        serverAddress = "203.0.113.9",
        serverPort = 443,
        uuid = "a1b2c3d4-e5f6-4890-abcd-ef1234567890",
        security = XraySecurity.Reality(serverName = "www.example.com", publicKey = "key", shortId = "ab"),
        deployedAtMillis = 1_700_000_000_000L,
    )

    /** Exactly what earlier versions wrote: today's profile minus the id they never had. */
    private fun legacyPayload(): String {
        val fields = XrayJson.default.encodeToJsonElement(XrayVpnProfile.serializer(), legacyProfile).jsonObject
        return JsonObject(fields - "id").toString()
    }

    private fun TestScope.store(storage: AppStorage) =
        StoredXraySubscriptionStore(storage, StandardTestDispatcher(testScheduler))

    @Test
    fun `starts empty when nothing is stored`() = runTest {
        assertEquals(emptyList<XraySubscription>(), store(InMemoryAppStorage()).subscriptions.first())
    }

    @Test
    fun `migrates the legacy profile into one subscription exactly once`() = runTest {
        val storage = InMemoryAppStorage().apply {
            putString(StorageDomain.XRAY_PROFILES, LEGACY_KEY, legacyPayload())
        }

        val migrated = store(storage).subscriptions.first()

        val server = migrated.single().servers.single()
        assertEquals(legacyProfile.copy(id = server.id), server)
        assertTrue(server.id.isNotBlank())
        assertNotEquals(migrated.single().id, server.id)
        assertNull(storage.getString(StorageDomain.XRAY_PROFILES, LEGACY_KEY))

        // A fresh store must read the migrated document, not mint new ids from a second migration.
        assertEquals(migrated, store(storage).subscriptions.first())
    }

    @Test
    fun `an undecodable legacy profile is dropped instead of migrated`() = runTest {
        val storage = InMemoryAppStorage().apply {
            putString(StorageDomain.XRAY_PROFILES, LEGACY_KEY, "{not json")
        }

        assertEquals(emptyList<XraySubscription>(), store(storage).subscriptions.first())
        assertNull(storage.getString(StorageDomain.XRAY_PROFILES, LEGACY_KEY))
    }

    @Test
    fun `an update is visible to collectors and survives a fresh store over the same storage`() = runTest {
        val storage = InMemoryAppStorage()
        val store = store(storage)
        val added = XraySubscription(id = "home", name = "Home", servers = listOf(legacyProfile))

        store.update { it + added }

        assertEquals(listOf(added), store.subscriptions.first())
        assertEquals(listOf(added), store(storage).subscriptions.first())
    }

    private class InMemoryAppStorage : AppStorage {
        private val values = mutableMapOf<Pair<StorageDomain, String>, String>()

        override fun getString(domain: StorageDomain, key: String): String? = values[domain to key]

        override fun putString(domain: StorageDomain, key: String, value: String) {
            values[domain to key] = value
        }

        override fun getBoolean(domain: StorageDomain, key: String): Boolean = getString(domain, key) == "true"

        override fun putBoolean(domain: StorageDomain, key: String, value: Boolean) {
            putString(domain, key, value.toString())
        }

        override fun remove(domain: StorageDomain, key: String) {
            values.remove(domain to key)
        }
    }

    private companion object {
        const val LEGACY_KEY = "active-profile"
    }
}
