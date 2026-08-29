package app.nukemichi.android.core.ssh.model

import app.nukemichi.android.core.security.Secret

sealed class SshAuth {
    data class Password(val password: Secret) : SshAuth()
    data class PrivateKey(val content: Secret, val passphrase: Secret? = null) : SshAuth()
}
