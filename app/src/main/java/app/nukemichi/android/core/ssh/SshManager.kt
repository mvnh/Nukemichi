package app.nukemichi.android.core.ssh

import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig

interface SshManager {

    suspend fun <T> withConnection(
        config: SshConfig,
        auth: SshAuth,
        block: suspend (SshConnection) -> Result<T>,
    ): Result<T>
}
