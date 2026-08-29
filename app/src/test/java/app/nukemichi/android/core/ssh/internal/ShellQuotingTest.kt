package app.nukemichi.android.core.ssh.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellQuotingTest {

    @Test
    fun `quotes a plain argument`() {
        assertEquals("bash 'echo hi'", renderCommand("bash", listOf("echo hi")))
    }

    @Test
    fun `escapes embedded single quotes`() {
        val script = "echo 'Deployment requires a root SSH user.' >&2"

        val rendered = renderCommand("bash", listOf("-c", script))

        assertEquals("bash '-c' 'echo '\\''Deployment requires a root SSH user.'\\'' >&2'", rendered)
    }
}
