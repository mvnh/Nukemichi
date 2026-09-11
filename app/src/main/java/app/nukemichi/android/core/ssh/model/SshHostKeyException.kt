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
