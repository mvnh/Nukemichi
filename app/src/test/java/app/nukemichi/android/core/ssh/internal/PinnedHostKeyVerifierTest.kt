package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.internal.util.SecurityUtils
import app.nukemichi.android.core.ssh.model.SshHostKeyChangedException
import app.nukemichi.android.core.ssh.model.SshHostKeyUnverifiableException
import app.nukemichi.android.core.ssh.model.SshUntrustedHostException
import net.schmizz.sshj.common.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.PublicKey
import java.util.Base64

/**
 * The distinction these tests exist for: a host presenting a key that disagrees with the pin is
 * the signature of an interception, and must not reach the user as the same routine
 * "first connection" prompt a genuinely unknown host does. Everything else here is the plumbing
 * that keeps that distinction honest.
 */
class PinnedHostKeyVerifierTest {

    @Test
    fun `an unreadable pin is reported as neither a first connection nor a changed key`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Unreadable, acceptedFingerprint = null)

        val error = runCatching { verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)) }.exceptionOrNull()

        assertTrue(
            "treating it as first contact would hide that this host was pinned once",
            error is SshHostKeyUnverifiableException,
        )
        assertEquals(ED25519_FINGERPRINT, (error as SshHostKeyUnverifiableException).fingerprint)
    }

    @Test
    fun `an unreadable pin still yields to a fingerprint the user accepted`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Unreadable, acceptedFingerprint = ED25519_FINGERPRINT)

        assertTrue(
            "without this, an invalidated Keystore key locks the user out of their own server",
            verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)),
        )
        assertEquals(ED25519_FINGERPRINT, verifier.verifiedFingerprint)
    }

    @Test
    fun `an unknown host with no pin is reported as a first connection`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.None, acceptedFingerprint = null)

        val error = runCatching { verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)) }.exceptionOrNull()

        assertTrue("a host with no pin is first contact, not a changed key", error is SshUntrustedHostException)
        assertEquals(ED25519_FINGERPRINT, (error as SshUntrustedHostException).fingerprint)
    }

    @Test
    fun `a key that disagrees with the pin is reported as changed, not as first contact`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Known(RSA_FINGERPRINT), acceptedFingerprint = null)

        val error = runCatching { verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)) }.exceptionOrNull()

        assertTrue(
            "a pinned host presenting a different key is the interception case",
            error is SshHostKeyChangedException,
        )
        error as SshHostKeyChangedException
        assertEquals(ED25519_FINGERPRINT, error.fingerprint)
        assertEquals(RSA_FINGERPRINT, error.expectedFingerprint)
    }

    @Test
    fun `a key matching the pin verifies`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Known(ED25519_FINGERPRINT), acceptedFingerprint = null)

        assertTrue(verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)))
        assertEquals(ED25519_FINGERPRINT, verifier.verifiedFingerprint)
    }

    @Test
    fun `a fingerprint the user accepted verifies even against a stale pin`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Known(RSA_FINGERPRINT), ED25519_FINGERPRINT)

        assertTrue("accepting a rotated key is the whole point of the changed-key prompt", verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)))
        assertEquals(ED25519_FINGERPRINT, verifier.verifiedFingerprint)
    }

    @Test
    fun `an accepted fingerprint that does not match the key still fails`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.None, acceptedFingerprint = RSA_FINGERPRINT)

        val error = runCatching { verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)) }.exceptionOrNull()

        assertTrue(error is SshUntrustedHostException)
    }

    @Test
    fun `no fingerprint is published for a key that never verified`() {
        val verifier = PinnedHostKeyVerifier(HostKeyPin.Known(RSA_FINGERPRINT), acceptedFingerprint = null)

        runCatching { verifier.verify(HOST, PORT, publicKey(ED25519_WIRE_BLOB)) }

        assertNull(
            "publishing here would let the caller pin a key it rejected",
            verifier.verifiedFingerprint,
        )
    }

    private fun publicKey(wireBlob: String): PublicKey =
        Buffer.PlainBuffer(Base64.getDecoder().decode(wireBlob)).readPublicKey()

    private companion object {
        const val HOST = "vps.example.com"
        const val PORT = 22

        const val RSA_WIRE_BLOB =
            "AAAAB3NzaC1yc2EAAAADAQABAAABAQDnEJCs2CtIkTI+1ngercWqb15QmRhJZoYeHr81eK2ZJczu" +
                "OFH5Jcu3k5OIF7P2OPgP7WWT9tIYaAkfOZ+hERGCr6qqKuKUUZ9IBOxfgT+L/MdDQoTPqomh/44a" +
                "uG5DcNNa78x9zET8C+WotmjH/fV3TdJBTTUzC++OSGzja6NKEPFvmSzGQWksTyFU+hBg7RjWPWDf" +
                "uZmA8RZ9TPFGCPwaFyDXzGUlLdCTiitQp2yoNTj3XpAfa18GAT8WhgiyvhcZQjGu1YrI1o09D5Zd" +
                "imEcvrRzFKUWh9g7TUPwAvZGLsS/DlHKSM6wnZH82Q7EM0SZLgrpcjbdFA1RUEgHvdvV"

        const val ED25519_WIRE_BLOB =
            "AAAAC3NzaC1lZDI1NTE5AAAAIOFYTGsCfsH+OVs1tKL6hkCOa5KH+UDdVfdP3mGCVHGv"

        // Not literals: SecurityUtilsTest already pins these against ssh-keygen, and restating
        // them here would only add a second place to update.
        val RSA_FINGERPRINT: String =
            SecurityUtils.getFingerprint(Buffer.PlainBuffer(Base64.getDecoder().decode(RSA_WIRE_BLOB)).readPublicKey())
        val ED25519_FINGERPRINT: String =
            SecurityUtils.getFingerprint(Buffer.PlainBuffer(Base64.getDecoder().decode(ED25519_WIRE_BLOB)).readPublicKey())
    }
}
