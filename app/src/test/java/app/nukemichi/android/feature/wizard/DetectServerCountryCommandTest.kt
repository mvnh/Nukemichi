package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.DetectServerCountryCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectServerCountryCommandTest {

    private fun parse(stdout: String) =
        DetectServerCountryCommand().parseOutput(CommandResult(stdout = stdout, stderr = "", exitCode = 0))

    @Test
    fun `accepts a bare two-letter code with the trailing newline services send`() {
        assertEquals("DE", parse("DE\n"))
        assertEquals("NL", parse("  NL  \n"))
    }

    /** The response comes from a third-party service over the network and ends up rendered in the UI. */
    @Test
    fun `rejects anything that is not exactly an uppercase alpha-2 code`() {
        listOf("", "\n", "de", "DEU", "D", "D1", "DE FR", "DE\nFR", "<html>", "{\"country\":\"DE\"}", "🇩🇪").forEach { stdout ->
            assertNull("'$stdout' must not parse as a country", parse(stdout))
        }
    }
}
