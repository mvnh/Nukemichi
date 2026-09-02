package app.nukemichi.android.core.storage

/**
 * A value is present in encrypted storage but can no longer be decrypted, which happens when the
 * Keystore key backing it is invalidated (a lock-screen change, a restore onto another device).
 *
 * It is deliberately distinct from a missing value. A pinned SSH host key that quietly disappears
 * turns the next connection into a fresh trust-on-first-use prompt that looks routine, so the read
 * fails loudly and the ciphertext stays on disk rather than being cleaned up.
 */
class SecureStorageUnreadableException(
    key: String,
    cause: Throwable,
) : Exception("Stored value for \"$key\" can no longer be decrypted.", cause)
