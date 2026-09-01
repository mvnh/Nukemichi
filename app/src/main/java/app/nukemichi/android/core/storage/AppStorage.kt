package app.nukemichi.android.core.storage

interface AppStorage {
    /** @throws SecureStorageUnreadableException if an encrypted value is present but undecryptable. */
    fun getString(domain: StorageDomain, key: String): String?
    fun putString(domain: StorageDomain, key: String, value: String)
    fun remove(domain: StorageDomain, key: String)
}

enum class StorageDomain(
    val keyPrefix: String,
    val encrypted: Boolean,
) {
    EXPERIENCE("experience", false),
    SSH_TRUST("ssh-trust", true),
    XRAY_PROFILES("xray-profiles", true),
}
