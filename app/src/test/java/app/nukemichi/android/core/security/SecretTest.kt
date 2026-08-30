package app.nukemichi.android.core.security

import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * [Secret] exists so that a secret cannot reach a log or a crash report by accident. The masking
 * only pays off if it survives being nested inside the structures that actually get printed, which
 * is what the state-level assertions below cover: a `data class` builds its own `toString` from its
 * properties, so the guarantee is only as good as Kotlin's dispatch to the value class's override.
 */
class SecretTest {

    @Test
    fun `toString masks the value`() {
        assertEquals("Secret(***)", Secret("hunter2").toString())
    }

    @Test
    fun `the value itself is preserved`() {
        assertEquals("hunter2", Secret("hunter2").value)
    }

    @Test
    fun `string interpolation goes through the masking override`() {
        assertFalse("${Secret("hunter2")}".contains("hunter2"))
    }

    /**
     * The wizard's state holds both the SSH password and the private key for the whole session and
     * is the single largest object in the app carrying secrets — in a debug build it is exactly the
     * kind of thing that ends up in a log line while someone is chasing an unrelated bug.
     */
    @Test
    fun `wizard state does not leak its secrets when printed`() {
        val state = WizardContract.State(
            password = Secret(PASSWORD),
            sshKey = Secret(PRIVATE_KEY),
            serverAddress = "192.0.2.10",
        )

        val printed = state.toString()

        assertFalse("password leaked into state.toString()", printed.contains(PASSWORD))
        assertFalse("private key leaked into state.toString()", printed.contains(PRIVATE_KEY))
        // Non-secret fields should still be readable, or the masking has gone too far to debug with.
        org.junit.Assert.assertTrue(printed.contains("192.0.2.10"))
    }

    private companion object {
        const val PASSWORD = "correct-horse-battery-staple"
        const val PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----b3BlbnNzaC1rZXk"
    }
}
