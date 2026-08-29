package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.LpfHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder
import timber.log.Timber
import java.io.IOException
import java.net.ServerSocket
import kotlin.concurrent.thread

internal class SshjLpfHandle(
    private val serverSocket: ServerSocket,
    private val forwarder: LocalPortForwarder,
    private val ioDispatcher: CoroutineDispatcher
) : LpfHandle {

    override val localPort: Int = serverSocket.localPort

    @Volatile
    private var isStopped = false

    fun start() {
        thread(name = "sshj-lpf-$localPort") {
            try {
                forwarder.listen()
            } catch (e: IOException) {
                if (!isStopped) {
                    Timber.e(e, "SSH local port forwarder stopped unexpectedly")
                }
            } finally {
                stopInternal()
            }
        }
    }

    override suspend fun stop() = withContext(ioDispatcher) {
        isStopped = true
        stopInternal()
    }

    private fun stopInternal() {
        runCatching {
            if (!serverSocket.isClosed) {
                serverSocket.close()
            }
        }
    }
}
