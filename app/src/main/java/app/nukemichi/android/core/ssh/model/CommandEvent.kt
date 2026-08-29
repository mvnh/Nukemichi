package app.nukemichi.android.core.ssh.model

sealed class CommandEvent {
    data class Output(val line: String) : CommandEvent()
    data class Exit(val code: Int) : CommandEvent()
    data class Error(val message: String) : CommandEvent()
}
