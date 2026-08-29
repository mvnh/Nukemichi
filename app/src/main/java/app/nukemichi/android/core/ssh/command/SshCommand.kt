package app.nukemichi.android.core.ssh.command

import app.nukemichi.android.core.ssh.model.CommandResult

interface SshCommand<T> {
    val command: String
    val args: List<String> get() = emptyList()

    fun parseOutput(result: CommandResult): T
}

interface BashScriptCommand<T> : SshCommand<T> {
    val script: String

    override val command: String get() = "bash"
    override val args: List<String> get() = listOf("-c", script)
}
