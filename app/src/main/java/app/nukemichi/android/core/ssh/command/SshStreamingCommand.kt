package app.nukemichi.android.core.ssh.command

interface SshStreamingCommand<T> {
    val command: String
    val args: List<String> get() = emptyList()

    fun parseLine(line: String): T?
}

interface BashScriptStreamingCommand<T> : SshStreamingCommand<T> {
    val script: String

    override val command: String get() = "bash"
    override val args: List<String> get() = listOf("-c", script)
}