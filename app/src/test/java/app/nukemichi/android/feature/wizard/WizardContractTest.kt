package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.security.Secret
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract
import app.nukemichi.android.feature.wizard.impl.ui.mvi.isConnectionProfileValid
import app.nukemichi.android.feature.wizard.impl.ui.mvi.isSshValid
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WizardContractTest {
    @Test
    fun `SSH validation requires a valid port and credentials`() {
        val state = WizardContract.State(
            serverAddress = "server.example",
            password = Secret("secret"),
            sshPort = "22",
            username = "root",
        )

        assertTrue(state.isSshValid)
        assertFalse(state.copy(sshPort = "70000").isSshValid)
        assertFalse(state.copy(password = Secret("")).isSshValid)
    }

    @Test
    fun `profile validation requires server-generated credentials`() {
        val state = WizardContract.State(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            realityPublicKey = "public-key",
            realityShortId = "abcd",
            realityServerName = "example.com",
        )

        assertTrue(state.isConnectionProfileValid)
        assertFalse(state.copy(uuid = "invalid").isConnectionProfileValid)
        assertFalse(state.copy(realityPublicKey = "").isConnectionProfileValid)
        assertFalse(state.copy(realityShortId = "").isConnectionProfileValid)
        assertFalse(state.copy(realityServerName = "").isConnectionProfileValid)
    }
}
