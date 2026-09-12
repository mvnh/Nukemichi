package app.nukemichi.android.core.ssh.model

/**
 * A host key that could not be accepted. The two cases are deliberately distinct types rather
 * than one type with a nullable field: they carry the same fingerprint but call for opposite
 * reactions from the user, and a caller that forgets to branch should not compile.
 */
sealed class SshHostKeyException(message: String) : Exception(message) {
    /** What the host actually presented. */
    abstract val fingerprint: String
}

/** No pin and no accepted fingerprint for this host yet: trust-on-first-use territory. */
class SshUntrustedHostException(
    override val fingerprint: String,
) : SshHostKeyException("Host key is not trusted. Fingerprint: $fingerprint")

/**
 * The host presented a key that disagrees with the one pinned for it. Benign when the server was
 * rebuilt, and the signature of an interception otherwise — which is why it is not the same type
 * as a first connection.
 */
class SshHostKeyChangedException(
    override val fingerprint: String,
    val expectedFingerprint: String,
) : SshHostKeyException(
    "Host key changed. Expected: $expectedFingerprint, got: $fingerprint"
)

/**
 * A pin exists for this host but can no longer be decrypted, so there is nothing to compare
 * against — see [SecureStorageUnreadableException][app.nukemichi.android.core.storage.SecureStorageUnreadableException].
 * Not the same as having no pin: the user verified this host once, and saying otherwise would
 * hand them the prompt they are likeliest to wave through.
 */
class SshHostKeyUnverifiableException(
    override val fingerprint: String,
) : SshHostKeyException(
    "The saved fingerprint for this host can no longer be read. Host is presenting: $fingerprint"
)
