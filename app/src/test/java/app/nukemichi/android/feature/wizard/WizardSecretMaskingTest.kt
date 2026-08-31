package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ui.util.UiSecret
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WizardSecretMaskingTest {

    @Test
    fun `password intent does not leak when printed`() {
        val printed = WizardContract.Intent.PasswordChanged(UiSecret(PASSWORD)).toString()

        assertFalse("password leaked into the intent's toString()", printed.contains(PASSWORD))
    }

    @Test
    fun `ssh key intent does not leak when printed`() {
        val printed = WizardContract.Intent.SshKeyChanged(UiSecret(PRIVATE_KEY)).toString()

        assertFalse("private key leaked into the intent's toString()", printed.contains(PRIVATE_KEY))
    }

    @Test
    fun `wizard state does not leak its secrets when printed`() {
        val state = WizardContract.State(
            password = UiSecret(PASSWORD),
            sshKey = UiSecret(PRIVATE_KEY),
            serverAddress = "192.0.2.10",
        )

        val printed = state.toString()

        assertFalse("password leaked into state.toString()", printed.contains(PASSWORD))
        assertFalse("private key leaked into state.toString()", printed.contains(PRIVATE_KEY))
        assertTrue("non-secret fields must stay readable, or this is undebuggable", printed.contains("192.0.2.10"))
    }

    @Test
    fun `the value survives the wrapper`() {
        assertEquals(PASSWORD, UiSecret(PASSWORD).value)
        assertEquals("", UiSecret.Empty.value)
    }

    private companion object {
        const val PASSWORD = "correct-horse-battery-staple"
        const val PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----b3BlbnNzaC1rZXk"
    }
}
