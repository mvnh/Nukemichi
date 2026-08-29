package app.nukemichi.android.core.ssh.internal.util

import net.schmizz.sshj.common.Buffer
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64

object SecurityUtils {

    // `PublicKey.encoded` is X.509 DER, not what SSH hashes — this must be the SSH wire-format
    // key (same bytes `ssh-keygen -lf`/a host's own fingerprint page hash) or TOFU has nothing to compare against.
    fun getFingerprint(hostKey: PublicKey): String {
        val wireEncoded = Buffer.PlainBuffer().putPublicKey(hostKey).compactData
        val digest = MessageDigest.getInstance("SHA-256").digest(wireEncoded)
        return "SHA256:" + Base64.getEncoder().withoutPadding().encodeToString(digest)
    }

    /** One-way digest of secret material, for use as a cache key that must not retain the secret. */
    fun getFingerprint(secret: String): String = Base64.getEncoder()
        .encodeToString(MessageDigest.getInstance("SHA-256").digest(secret.toByteArray(Charsets.UTF_8)))
}