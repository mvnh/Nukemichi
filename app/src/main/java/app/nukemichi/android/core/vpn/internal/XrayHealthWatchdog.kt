package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.di.IoDispatcher
import app.nukemichi.android.core.vpn.SocksEndpoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
internal class XrayHealthWatchdog @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope, socksEndpoint: SocksEndpoint, onDegraded: suspend () -> Unit) {
        stop()
        job = scope.launch(ioDispatcher) {
            var consecutiveFailures = 0
            while (isActive) {
                delay(PROBE_INTERVAL_MS.milliseconds)
                val healthy = probe(socksEndpoint)
                consecutiveFailures = if (healthy) 0 else consecutiveFailures + 1
                if (consecutiveFailures >= CONSECUTIVE_FAILURES_THRESHOLD) {
                    Timber.w(
                        "XrayHealthWatchdog: %d consecutive failed probes, reporting degraded",
                        consecutiveFailures,
                    )
                    onDegraded()
                    break
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun probe(socksEndpoint: SocksEndpoint): Boolean = try {
        Socket().use { socket ->
            socket.soTimeout = PROBE_TIMEOUT_MS.toInt()
            socket.connect(InetSocketAddress(socksEndpoint.host, socksEndpoint.port), PROBE_TIMEOUT_MS.toInt())
            Socks5Client.connect(socket, socksEndpoint.username, socksEndpoint.password, PROBE_HOST, PROBE_PORT)
        }
        true
    } catch (error: IOException) {
        false
    } catch (error: IllegalStateException) {
        false
    }

    private companion object {
        const val PROBE_INTERVAL_MS = 15_000L
        const val PROBE_TIMEOUT_MS = 6_000L
        const val CONSECUTIVE_FAILURES_THRESHOLD = 2

        const val PROBE_HOST = "gstatic.com"
        const val PROBE_PORT = 443
    }
}
