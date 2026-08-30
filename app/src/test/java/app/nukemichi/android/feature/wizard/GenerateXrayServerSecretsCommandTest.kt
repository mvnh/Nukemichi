package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.GenerateXrayServerSecretsCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.InstallXrayRuntimeCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateXrayServerSecretsCommandTest {
    @Test
    fun `parses server-generated secrets`() {
        val result = CommandResult(
            stdout = """
                UUID=123e4567-e89b-12d3-a456-426614174000
                PRIVATE_KEY=private-key
                PUBLIC_KEY=public-key
                SHORT_ID=abcd1234
            """.trimIndent(),
            stderr = "",
            exitCode = 0,
        )

        val secrets = GenerateXrayServerSecretsCommand().parseOutput(result)

        assertEquals("123e4567-e89b-12d3-a456-426614174000", secrets.uuid)
        assertEquals("private-key", secrets.privateKey)
        assertEquals("public-key", secrets.publicKey)
        assertEquals("abcd1234", secrets.shortId)
    }

    @Test
    fun `maps known architectures to xray release assets`() {
        assertEquals("Xray-linux-64.zip", InstallXrayRuntimeCommand.releaseAssetFor("64").name)
        assertEquals("Xray-linux-arm64-v8a.zip", InstallXrayRuntimeCommand.releaseAssetFor("arm64-v8a").name)
        assertThrows(IllegalStateException::class.java) {
            InstallXrayRuntimeCommand.releaseAssetFor("mips")
        }
    }
}
