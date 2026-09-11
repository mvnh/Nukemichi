package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.internal.util.SecurityUtils
import app.nukemichi.android.core.ssh.model.SshHostKeyChangedException
import app.nukemichi.android.core.ssh.model.SshUntrustedHostException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import timber.log.Timber
import java.security.PublicKey

/**
 * Decides whether a host key may be used, and which of the two rejections the caller has to
 * explain.
 *
 * [pinnedFingerprint] is what trust-on-first-use stored for this host; [acceptedFingerprint] is
 * what the user approved for this one attempt. Accepting is checked first so a genuinely rotated
 * key can be adopted, but only ever through a decision the user made against a prompt that told
 * them the old key existed — a stale pin alone never yields to a new key.
 */
internal class PinnedHostKeyVerifier(
    private val pinnedFingerprint: String?,
    private val acceptedFingerprint: String?,
) : HostKeyVerifier {

    /** Set only once a key has been accepted, so a caller cannot pin one this rejected. */
    var verifiedFingerprint: String? = null
        private set

    override fun verify(hostname: String, port: Int, key: PublicKey): Boolean {
        val actualFingerprint = SecurityUtils.getFingerprint(key)

        return when {
            actualFingerprint == acceptedFingerprint || actualFingerprint == pinnedFingerprint -> {
                verifiedFingerprint = actualFingerprint
                true
            }

            pinnedFingerprint != null -> {
                Timber.w(
                    "Host key changed: %s:%d expected=%s actual=%s",
                    hostname,
                    port,
                    pinnedFingerprint,
                    actualFingerprint,
                )
                throw SshHostKeyChangedException(actualFingerprint, pinnedFingerprint)
            }

            else -> {
                Timber.w("Untrusted host key: %s:%d fingerprint=%s", hostname, port, actualFingerprint)
                throw SshUntrustedHostException(actualFingerprint)
            }
        }
    }

    override fun findExistingAlgorithms(hostname: String, port: Int): List<String?>? = null
}
