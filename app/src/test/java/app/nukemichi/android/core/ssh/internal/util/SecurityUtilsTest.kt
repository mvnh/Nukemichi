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
 * Golden tests: the expected fingerprints below were produced by OpenSSH itself
 * (`ssh-keygen -lf key.pub`) for the keys embedded here, so they pin interoperability with the
 * rest of the world rather than merely with this implementation.
 *
 * That matters because the fingerprint is the whole of TOFU. If the encoding drifts — the classic
 * slip being to hash `PublicKey.encoded`, which is X.509 DER, instead of the SSH wire format — the
 * app keeps working against its own stored values while silently disagreeing with every fingerprint
 * a user could check against their hosting panel or `ssh-keyscan`. Wrong-but-self-consistent is the
 * failure mode these tests exist to catch, and it is invisible without an external oracle.
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

    /**
     * [SecurityUtils.getFingerprint] over a secret exists to key a connection cache without the
     * cache retaining the secret — so the digest must not echo its input back.
     */
    @Test
    fun `secret digests do not leak the secret`() {
        val secret = "correct-horse-battery-staple"

        val digest = SecurityUtils.getFingerprint(secret)

        assertFalse(digest.contains(secret))
    }

    private fun publicKey(wireBlob: String): PublicKey =
        Buffer.PlainBuffer(Base64.getDecoder().decode(wireBlob)).readPublicKey()

    private companion object {
        /** Body of an `ssh-rsa` line in an OpenSSH `.pub` file — the SSH wire-format key blob. */
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
