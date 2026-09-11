package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.internal.util.SecurityUtils
import app.nukemichi.android.core.ssh.model.SshHostKeyChangedException
import app.nukemichi.android.core.ssh.model.SshHostKeyUnverifiableException
import app.nukemichi.android.core.ssh.model.SshUntrustedHostException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import timber.log.Timber
import java.security.PublicKey

/**
 * Decides whether a host key may be used, and which of the three rejections the caller has to
 * explain.
 *
 * [pin] is what trust-on-first-use knows about this host; [acceptedFingerprint] is what the user
 * approved for this one attempt. Accepting is checked first so a genuinely rotated key can be
 * adopted, but only ever through a decision the user made against a prompt that told them what
 * the old state was — a pin on its own never yields to a new key.
 */
internal class PinnedHostKeyVerifier(
    private val pin: HostKeyPin,
    private val acceptedFingerprint: String?,
) : HostKeyVerifier {

    /** Set only once a key has been accepted, so a caller cannot pin one this rejected. */
    var verifiedFingerprint: String? = null
        private set

    override fun verify(hostname: String, port: Int, key: PublicKey): Boolean {
        val actualFingerprint = SecurityUtils.getFingerprint(key)

        if (actualFingerprint == acceptedFingerprint || pin.matches(actualFingerprint)) {
            verifiedFingerprint = actualFingerprint
            return true
        }

        when (pin) {
            is HostKeyPin.Known -> {
                Timber.w(
                    "Host key changed: %s:%d expected=%s actual=%s",
                    hostname,
                    port,
                    pin.fingerprint,
                    actualFingerprint,
                )
                throw SshHostKeyChangedException(actualFingerprint, pin.fingerprint)
            }

            HostKeyPin.Unreadable -> {
                Timber.w("Pinned host key is undecryptable: %s:%d actual=%s", hostname, port, actualFingerprint)
                throw SshHostKeyUnverifiableException(actualFingerprint)
            }

            HostKeyPin.None -> {
                Timber.w("Untrusted host key: %s:%d fingerprint=%s", hostname, port, actualFingerprint)
                throw SshUntrustedHostException(actualFingerprint)
            }
        }
    }

    override fun findExistingAlgorithms(hostname: String, port: Int): List<String?>? = null
}
