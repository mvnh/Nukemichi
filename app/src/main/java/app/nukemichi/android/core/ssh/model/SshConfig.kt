package app.nukemichi.android.core.ssh.model

data class SshConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val expectedFingerprint: String? = null,
    val password: String? = null,
    val privateKey: String? = null,
    val privateKeyPassphrase: String? = null
)
