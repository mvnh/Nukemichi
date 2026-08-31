package app.nukemichi.android.core.ssh.internal.util

import net.schmizz.sshj.common.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.PublicKey
import java.util.Base64

/**
 * Expected values come from `ssh-keygen -lf` on the embedded keys, so these pin interoperability
 * rather than restating the implementation. Wrong-but-self-consistent is the failure mode: hash
 * X.509 DER instead of SSH wire format and the app still matches its own stored values while
 * agreeing with nothing a user can check against their hosting panel.
 */
class SecurityUtilsTest {

    @Test
    fun `matches OpenSSH for an RSA host key`() {
        assertEquals(RSA_EXPECTED_FINGERPRINT, SecurityUtils.getFingerprint(publicKey(RSA_WIRE_BLOB)))
    }

    @Test
    fun `matches OpenSSH for an Ed25519 host key`() {
        assertEquals(ED25519_EXPECTED_FINGERPRINT, SecurityUtils.getFingerprint(publicKey(ED25519_WIRE_BLOB)))
    }

    @Test
    fun `formats fingerprints the way OpenSSH prints them`() {
        val fingerprint = SecurityUtils.getFingerprint(publicKey(ED25519_WIRE_BLOB))

        assertTrue("must carry the hash-algorithm prefix", fingerprint.startsWith("SHA256:"))
        assertFalse(
            "OpenSSH prints base64 unpadded; padding here would break every comparison",
            fingerprint.endsWith("="),
        )
    }

    @Test
    fun `different host keys produce different fingerprints`() {
        assertNotEquals(
            SecurityUtils.getFingerprint(publicKey(RSA_WIRE_BLOB)),
            SecurityUtils.getFingerprint(publicKey(ED25519_WIRE_BLOB)),
        )
    }

    @Test
    fun `secret digests are stable and collision-free across inputs`() {
        assertEquals(SecurityUtils.getFingerprint("hunter2"), SecurityUtils.getFingerprint("hunter2"))
        assertNotEquals(SecurityUtils.getFingerprint("hunter2"), SecurityUtils.getFingerprint("hunter3"))
    }

    @Test
    fun `secret digests do not leak the secret`() {
        val secret = "correct-horse-battery-staple"

        val digest = SecurityUtils.getFingerprint(secret)

        assertFalse(digest.contains(secret))
    }

    private fun publicKey(wireBlob: String): PublicKey =
        Buffer.PlainBuffer(Base64.getDecoder().decode(wireBlob)).readPublicKey()

    private companion object {
        /** The blob from an OpenSSH `.pub` line, i.e. SSH wire format. */
        const val RSA_WIRE_BLOB =
            "AAAAB3NzaC1yc2EAAAADAQABAAABAQDnEJCs2CtIkTI+1ngercWqb15QmRhJZoYeHr81eK2ZJczu" +
                "OFH5Jcu3k5OIF7P2OPgP7WWT9tIYaAkfOZ+hERGCr6qqKuKUUZ9IBOxfgT+L/MdDQoTPqomh/44a" +
                "uG5DcNNa78x9zET8C+WotmjH/fV3TdJBTTUzC++OSGzja6NKEPFvmSzGQWksTyFU+hBg7RjWPWDf" +
                "uZmA8RZ9TPFGCPwaFyDXzGUlLdCTiitQp2yoNTj3XpAfa18GAT8WhgiyvhcZQjGu1YrI1o09D5Zd" +
                "imEcvrRzFKUWh9g7TUPwAvZGLsS/DlHKSM6wnZH82Q7EM0SZLgrpcjbdFA1RUEgHvdvV"
        const val RSA_EXPECTED_FINGERPRINT = "SHA256:rEOfmsaJfPbR6pRSUauv1RssZfuWtgjnHLxhkR/0fdA"

        const val ED25519_WIRE_BLOB =
            "AAAAC3NzaC1lZDI1NTE5AAAAIOFYTGsCfsH+OVs1tKL6hkCOa5KH+UDdVfdP3mGCVHGv"
        const val ED25519_EXPECTED_FINGERPRINT = "SHA256:13GdpOAwAajRoucdr8l4kRuwZl4RwhuPGRUM+lZqpwo"
    }
}
