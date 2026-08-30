package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.GenerateXrayServerSecretsCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.InstallXrayRuntimeCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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

    /**
     * These binaries get installed and executed as root on the user's VPS, so a digest that is
     * absent, truncated or a copy-paste of the other architecture's is a supply-chain hole rather
     * than a cosmetic slip — assert the shape here so an update can't quietly drop one.
     */
    @Test
    fun `pins a distinct full-length digest per architecture`() {
        val amd64 = InstallXrayRuntimeCommand.releaseAssetFor("64").sha256
        val arm64 = InstallXrayRuntimeCommand.releaseAssetFor("arm64-v8a").sha256

        listOf(amd64, arm64).forEach { digest ->
            assertEquals(64, digest.length)
            assertTrue("digest is not lowercase hex: $digest", digest.matches(Regex("[0-9a-f]{64}")))
        }
        assertNotEquals(amd64, arm64)
    }
}
