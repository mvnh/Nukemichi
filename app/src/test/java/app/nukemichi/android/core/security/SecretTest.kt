package app.nukemichi.android.core.security

import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

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

    // A data class builds its own toString from its properties, so the masking holds only if Kotlin
    // dispatches to the value class override rather than to the underlying String.
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
        org.junit.Assert.assertTrue(printed.contains("192.0.2.10"))
    }

    private companion object {
        const val PASSWORD = "correct-horse-battery-staple"
        const val PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----b3BlbnNzaC1rZXk"
    }
}
