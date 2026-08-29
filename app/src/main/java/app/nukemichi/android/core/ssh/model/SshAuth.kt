package app.nukemichi.android.core.ssh.model

sealed class SshAuth {
    data class Password(val password: String) : SshAuth()
    data class PrivateKey(val content: String, val passphrase: String? = null) : SshAuth()
}
