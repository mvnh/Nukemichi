package app.nukemichi.android.core.ssh.ext

import app.nukemichi.android.core.ssh.LpfHandle
import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.command.SshCommand
import app.nukemichi.android.core.ssh.model.CommandEvent
import app.nukemichi.android.core.ssh.model.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Commands here parse optimistically (`requireValue`, regex scraping), assuming they only ever see
 * output from a command that succeeded. The case that matters is therefore a non-zero exit
 * skipping `parseOutput` entirely, rather than a bogus success scraped out of error output.
 */
class SshConnectionExtTest {

    @Test
    fun `parses output when the command exits zero`() = runTest {
        val connection = FakeSshConnection(
            CommandEvent.Output("first"),
            CommandEvent.Output("second"),
            CommandEvent.Exit(0),
        )

        val result = connection.execute(RecordingCommand())

        assertEquals("first\nsecond\n", result.getOrThrow().stdout)
    }

    @Test
    fun `fails without parsing when the command exits non-zero`() = runTest {
        val command = RecordingCommand()
        val connection = FakeSshConnection(
            CommandEvent.Error("permission denied"),
            CommandEvent.Exit(13),
        )

        val result = connection.execute(command)

        assertTrue(result.isFailure)
        assertFalse("parseOutput must not see the output of a failed command", command.wasParsed)
    }

    @Test
    fun `surfaces the exit code and stderr in the failure message`() = runTest {
        val connection = FakeSshConnection(
            CommandEvent.Error("no such file"),
            CommandEvent.Exit(127),
        )

        val message = connection.execute(RecordingCommand()).exceptionOrNull()?.message.orEmpty()

        assertTrue("exit code missing from: $message", message.contains("127"))
        assertTrue("stderr missing from: $message", message.contains("no such file"))
    }

    /** The live terminal is fed from this callback, and stderr is what a failing step needs to show. */
    @Test
    fun `streams both stdout and stderr to the line callback in order`() = runTest {
        val connection = FakeSshConnection(
            CommandEvent.Output("downloading"),
            CommandEvent.Error("warning: slow mirror"),
            CommandEvent.Output("done"),
            CommandEvent.Exit(0),
        )
        val seen = mutableListOf<String>()

        connection.execute(RecordingCommand()) { seen += it }

        assertEquals(listOf("downloading", "warning: slow mirror", "done"), seen)
    }

    /** A stream that ends without an Exit event is a dropped connection, not a success. */
    @Test
    fun `fails when the command never reports an exit code`() = runTest {
        val connection = FakeSshConnection(CommandEvent.Output("partial"))

        assertTrue(connection.execute(RecordingCommand()).isFailure)
    }

    @Test
    fun `wraps a throwing parser into a failed result`() = runTest {
        val connection = FakeSshConnection(CommandEvent.Output("junk"), CommandEvent.Exit(0))

        val result = connection.execute(
            object : SshCommand<String> {
                override val command = "true"
                override fun parseOutput(result: CommandResult): String =
                    error("Server did not return UUID.")
            }
        )

        assertTrue(result.isFailure)
        assertEquals("Server did not return UUID.", result.exceptionOrNull()?.message)
    }
}

private class RecordingCommand : SshCommand<CommandResult> {
    override val command = "true"

    var wasParsed = false
        private set

    override fun parseOutput(result: CommandResult): CommandResult {
        wasParsed = true
        return result
    }
}

/** Replays a fixed event sequence; the rest of [SshConnection] is out of scope for this helper. */
private class FakeSshConnection(private vararg val events: CommandEvent) : SshConnection {
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(true)

    override fun refreshConnectionState(): Boolean = true

    override fun executeStreaming(command: String, args: List<String>): Flow<CommandEvent> =
        events.toList().asFlow()

    override suspend fun execute(command: String, args: List<String>): Result<CommandResult> =
        throw UnsupportedOperationException("the helper under test uses executeStreaming")

    override suspend fun upload(remotePath: String, content: ByteArray, permissions: Int?): Result<Unit> =
        throw UnsupportedOperationException()

    override suspend fun startLpf(remotePort: Int): Result<LpfHandle> =
        throw UnsupportedOperationException()

    override suspend fun disconnect() = Unit
}
