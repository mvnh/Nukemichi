package app.nukemichi.android.core.ssh.internal.model

import app.nukemichi.android.core.security.Secret
import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * [SessionKey] decides when SshjManager hands back a pooled connection instead of opening a new
 * one, so anything that changes who you authenticate as, or how, has to change the key — otherwise
 * a second set of credentials silently rides on the first one's session.
 *
 * Note what is deliberately *not* asserted here: `SshConfig.expectedFingerprint` is not part of the
 * key today, so two calls that disagree about which host key to trust can share a pooled
 * connection. Nothing in the current wizard flow varies the fingerprint mid-session, but it is a
 * trust decision living outside the cache key and worth revisiting.
 */
class SessionKeyTest {

    @Test
    fun `same host and credentials produce the same key`() {
        assertEquals(key(), key())
    }

    @Test
    fun `host is compared case-insensitively`() {
        assertEquals(
            key(config(host = "Server.Example.COM")),
            key(config(host = "server.example.com")),
        )
    }

    @Test
    fun `a different host, port or user is a different session`() {
        assertNotEquals(key(), key(config(host = "other.example")))
        assertNotEquals(key(), key(config(port = 2222)))
        assertNotEquals(key(), key(config(username = "deploy")))
    }

    @Test
    fun `a different password is a different session`() {
        assertNotEquals(key(), key(auth = SshAuth.Password(Secret("other"))))
    }

    @Test
    fun `a key and a password are never the same session`() {
        assertNotEquals(
            key(auth = SshAuth.Password(Secret("same"))),
            key(auth = SshAuth.PrivateKey(Secret("same"))),
        )
    }

    @Test
    fun `a different private key or passphrase is a different session`() {
        val base = SshAuth.PrivateKey(Secret("KEY-A"))

        assertNotEquals(key(auth = base), key(auth = SshAuth.PrivateKey(Secret("KEY-B"))))
        assertNotEquals(key(auth = base), key(auth = SshAuth.PrivateKey(Secret("KEY-A"), Secret("pass"))))
    }

    /** The key is held for the lifetime of the pool, so it must not carry the credential itself. */
    @Test
    fun `the key does not retain the credential`() {
        val password = "correct-horse-battery-staple"

        val rendered = key(auth = SshAuth.Password(Secret(password))).toString()

        assertFalse(rendered.contains(password))
    }

    private fun config(
        host: String = "server.example.com",
        port: Int = 22,
        username: String = "root",
    ) = SshConfig(host = host, port = port, username = username)

    private fun key(
        config: SshConfig = config(),
        auth: SshAuth = SshAuth.Password(Secret("secret")),
    ) = SessionKey.of(config, auth)
}
