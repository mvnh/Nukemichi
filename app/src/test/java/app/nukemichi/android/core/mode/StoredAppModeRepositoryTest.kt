package app.nukemichi.android.core.mode

import app.nukemichi.android.core.mode.internal.StoredAppModeRepository
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredAppModeRepositoryTest {

    @Test
    fun `defaults to NORMAL when nothing is persisted`() {
        val repository = StoredAppModeRepository(InMemoryAppStorage())

        assertEquals(AppMode.NORMAL, repository.mode.value)
    }

    @Test
    fun `setMode is reflected immediately and survives a fresh repository over the same storage`() {
        val storage = InMemoryAppStorage()
        val repository = StoredAppModeRepository(storage)

        repository.setMode(AppMode.ADVANCED)
        assertEquals(AppMode.ADVANCED, repository.mode.value)

        val reloaded = StoredAppModeRepository(storage)
        assertEquals(AppMode.ADVANCED, reloaded.mode.value)
    }

    @Test
    fun `switching back to NORMAL persists too, not just the first choice`() {
        val storage = InMemoryAppStorage()
        val repository = StoredAppModeRepository(storage)

        repository.setMode(AppMode.ADVANCED)
        repository.setMode(AppMode.NORMAL)

        assertEquals(AppMode.NORMAL, StoredAppModeRepository(storage).mode.value)
    }

    /** Advanced mode unlocks a raw config editor and a server shell, so the fallback direction matters. */
    @Test
    fun `an unrecognized persisted value falls back to NORMAL`() {
        listOf("", " ", "TRUE", "yes", "1", "advanced", "{}").forEach { stored ->
            val storage = InMemoryAppStorage().apply {
                putString(StorageDomain.EXPERIENCE, ExperienceKeys.ADVANCED_MODE_ENABLED, stored)
            }

            assertEquals(
                "'$stored' must not be read as advanced mode",
                AppMode.NORMAL,
                StoredAppModeRepository(storage).mode.value,
            )
        }
    }

    private class InMemoryAppStorage : AppStorage {
        private val values = mutableMapOf<Pair<StorageDomain, String>, String>()

        override fun getString(domain: StorageDomain, key: String): String? = values[domain to key]

        override fun putString(domain: StorageDomain, key: String, value: String) {
            values[domain to key] = value
        }

        override fun remove(domain: StorageDomain, key: String) {
            values.remove(domain to key)
        }
    }
}
