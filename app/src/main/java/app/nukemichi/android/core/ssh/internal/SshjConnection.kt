package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.LpfHandle
import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.model.CommandEvent
import app.nukemichi.android.core.ssh.model.CommandResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Parameters
import net.schmizz.sshj.sftp.OpenMode
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.EnumSet
import kotlin.math.min

internal class SshjConnection(
    private val client: SSHClient,
    private val ioDispatcher: CoroutineDispatcher
) : SshConnection {

    private val connected = MutableStateFlow(client.isConnected)
    override val isConnected: StateFlow<Boolean> = connected.asStateFlow()

    override fun refreshConnectionState(): Boolean = client.isConnected.also { connected.value = it }

    private val lpfsMutex = Mutex()
    private val activeLpfs = mutableListOf<LpfHandle>()

    override suspend fun execute(
        command: String,
        args: List<String>
    ): Result<CommandResult> = try {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        var exitCode = -1
        executeStreaming(command, args).collect { event ->
            when (event) {
                is CommandEvent.Output -> stdout.appendLine(event.line)
                is CommandEvent.Error -> stderr.appendLine(event.message)
                is CommandEvent.Exit -> exitCode = event.code
            }
        }
        Result.success(CommandResult(stdout.toString(), stderr.toString(), exitCode))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override fun executeStreaming(
        command: String,
        args: List<String>
    ): Flow<CommandEvent> = channelFlow {
        Timber.d("SSH streaming command start: %s", command)
        val session = client.startSession()
        try {
            val cmd = session.exec(renderCommand(command, args))

            val stdoutJob = drainLines(cmd.inputStream) { trySend(CommandEvent.Output(it)) }
            val stderrJob = drainLines(cmd.errorStream) { trySend(CommandEvent.Error(it)) }

            stdoutJob.join()
            stderrJob.join()
            cmd.join()
            trySend(CommandEvent.Exit(cmd.exitStatus ?: -1))
            Timber.d("SSH streaming command finished: %s exit=%d", command, cmd.exitStatus ?: -1)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Timber.e(error, "SSH streaming command failed: %s", command)
            trySend(CommandEvent.Error(error.message ?: "Unknown execution error"))
        } finally {
            withContext(NonCancellable) { runCatching { session.close() } }
        }
    }.flowOn(ioDispatcher)

    private fun CoroutineScope.drainLines(
        stream: InputStream,
        onLine: (String) -> Unit,
    ): Job = launch(ioDispatcher) {
        try {
            stream.bufferedReader().useLines { lines -> lines.forEach(onLine) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: IOException) {
            Timber.d(error, "SSH stream closed while reading")
        }
    }

    override suspend fun upload(
        remotePath: String,
        content: ByteArray,
        permissions: Int?
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            client.newSFTPClient().use { sftp ->
                val flags = EnumSet.of(OpenMode.CREAT, OpenMode.WRITE, OpenMode.TRUNC)
                sftp.open(remotePath, flags).use { remoteFile ->
                    var offset = 0L
                    var index = 0
                    val chunkSize = 32 * 1024
                    while (index < content.size) {
                        val length = min(chunkSize, content.size - index)
                        remoteFile.write(offset, content, index, length)
                        index += length
                        offset += length.toLong()
                    }
                }

                if (permissions != null) {
                    sftp.chmod(remotePath, permissions)
                }
            }
            Timber.i("SSH upload completed: %s (%d bytes)", remotePath, content.size)
        }.onFailure { error ->
            Timber.e(error, "SSH upload failed: %s", remotePath)
        }
    }

    override suspend fun startLpf(remotePort: Int): Result<LpfHandle> = withContext(ioDispatcher) {
        runCatching {
            val localhost = InetAddress.getByName("127.0.0.1")
            val serverSocket = ServerSocket(0, 50, localhost)

            val params = Parameters(
                "127.0.0.1",
                serverSocket.localPort,
                "127.0.0.1",
                remotePort
            )

            val forwarder = client.newLocalPortForwarder(params, serverSocket)

            val handle = SshjLpfHandle(serverSocket, forwarder, ioDispatcher)
            handle.start()

            lpfsMutex.withLock { activeLpfs.add(handle) }
            handle
        }
    }

    override suspend fun disconnect() = withContext(ioDispatcher) {
        lpfsMutex.withLock {
            activeLpfs.forEach { it.stop() }
            activeLpfs.clear()
        }

        if (client.isConnected) {
            client.disconnect()
        }
        client.close()
        connected.value = false
    }

}
