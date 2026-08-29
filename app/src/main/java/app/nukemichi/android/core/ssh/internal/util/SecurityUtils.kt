package app.nukemichi.android.core.ssh.internal.util

import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64

object SecurityUtils {

    fun getFingerprint(hostKey: PublicKey): String = fingerprint(hostKey.encoded)

    /** One-way digest of secret material, for use as a cache key that must not retain the secret. */
    fun getFingerprint(secret: String): String = fingerprint(secret.toByteArray(Charsets.UTF_8))

    private fun fingerprint(bytes: ByteArray): String = Base64.getEncoder()
        .encodeToString(MessageDigest.getInstance("SHA-256").digest(bytes))
}