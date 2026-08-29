package app.nukemichi.android.core.ssh.ext

import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.command.SshCommand
import app.nukemichi.android.core.ssh.command.SshStreamingCommand
import app.nukemichi.android.core.ssh.model.CommandEvent
import app.nukemichi.android.core.ssh.model.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

/**
 * Runs [command] over the streaming exec channel, so [onOutputLine] receives every stdout/stderr
 * line as it arrives (used to drive live-terminal UI) while still returning the same typed result
 * as a plain one-shot execution once the process exits.
 */
suspend fun <T> SshConnection.execute(
    command: SshCommand<T>,
    onOutputLine: suspend (String) -> Unit = {},
): Result<T> {
    val stdout = StringBuilder()
    val stderr = StringBuilder()
    var exitCode = -1

    executeStreaming(command.command, command.args).collect { event ->
        when (event) {
            is CommandEvent.Output -> {
                stdout.appendLine(event.line)
                onOutputLine(event.line)
            }
            is CommandEvent.Error -> {
                stderr.appendLine(event.message)
                onOutputLine(event.message)
            }
            is CommandEvent.Exit -> exitCode = event.code
        }
    }

    return runCatching {
        check(exitCode == 0) { "Exit code $exitCode: $stderr" }
        command.parseOutput(CommandResult(stdout.toString(), stderr.toString(), exitCode))
    }
}

fun <T> SshConnection.executeStreaming(
    command: SshStreamingCommand<T>
): Flow<T> =
    executeStreaming(command.command, command.args)
        .mapNotNull { event ->
            when (event) {
                is CommandEvent.Output -> {
                    command.parseLine(event.line)
                }

                is CommandEvent.Error -> {
                    Timber.w("SSH Streaming stderr: %s", event.message)
                    null
                }

                is CommandEvent.Exit -> {
                    if (event.code != 0) {
                        Timber.e("SSH Streaming process exited with code %d", event.code)
                    }
                    null
                }
            }
        }