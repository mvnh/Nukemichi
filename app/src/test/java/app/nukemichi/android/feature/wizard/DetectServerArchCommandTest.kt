package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.DetectServerArchCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DetectServerArchCommandTest {

    private fun result(stdout: String) = CommandResult(stdout = stdout, stderr = "", exitCode = 0)

    @Test
    fun `maps x86_64 to the xray release arch`() {
        assertEquals("64", DetectServerArchCommand().parseOutput(result("x86_64\n")))
        assertEquals("64", DetectServerArchCommand().parseOutput(result("amd64\n")))
    }

    @Test
    fun `maps aarch64 to the xray release arch`() {
        assertEquals("arm64-v8a", DetectServerArchCommand().parseOutput(result("aarch64\n")))
        assertEquals("arm64-v8a", DetectServerArchCommand().parseOutput(result("arm64\n")))
    }

    @Test
    fun `rejects an unsupported architecture`() {
        assertThrows(IllegalStateException::class.java) {
            DetectServerArchCommand().parseOutput(result("mips\n"))
        }
    }
}
