package app.nukemichi.android.core.ssh

import app.nukemichi.android.core.ssh.model.CommandEvent
import app.nukemichi.android.core.ssh.model.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SshConnection {
    val isConnected: StateFlow<Boolean>

    suspend fun execute(
        command: String,
        args: List<String> = emptyList()
    ): Result<CommandResult>

    fun executeStreaming(
        command: String,
        args: List<String> = emptyList()
    ): Flow<CommandEvent>

    suspend fun upload(
        remotePath: String,
        content: ByteArray,
        permissions: Int? = null
    ): Result<Unit>

    suspend fun startLpf(remotePort: Int): Result<LpfHandle>
    suspend fun disconnect()
}
