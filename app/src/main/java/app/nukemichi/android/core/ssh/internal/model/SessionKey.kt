package app.nukemichi.android.core.ssh.internal.model

import app.nukemichi.android.core.ssh.internal.util.SecurityUtils
import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig
import java.util.Locale

internal data class SessionKey(
    val host: String,
    val port: Int,
    val username: String,
    val authFingerprint: String,
) {
    companion object {
        fun of(config: SshConfig, auth: SshAuth) = SessionKey(
            host = config.host.lowercase(Locale.ROOT),
            port = config.port,
            username = config.username,
            authFingerprint = auth.fingerprint(),
        )

        private fun SshAuth.fingerprint(): String {
            val nul = 0.toChar()
            return SecurityUtils.getFingerprint(
                when (this) {
                    is SshAuth.Password -> "password$nul${password.value}"
                    is SshAuth.PrivateKey -> "key$nul${content.value}$nul${passphrase?.value.orEmpty()}"
                }
            )
        }
    }
}
