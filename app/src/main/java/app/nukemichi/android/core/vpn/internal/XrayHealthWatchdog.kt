package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.vpn.ProbeTargets
import app.nukemichi.android.core.vpn.SocksEndpoint
import app.nukemichi.android.platform.di.IoDispatcher
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

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
                // Jittered, so the tunnel does not emit a probe on the same beat forever.
                delay((PROBE_INTERVAL_MS + Random.nextLong(PROBE_JITTER_MS)).milliseconds)
                val healthy = probeRound(socksEndpoint)
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

    /**
     * A round fails only when two different hosts both fail. One name being blocked - which is
     * routine on the networks this app exists for - is not the tunnel being dead, and treating it
     * as such used to mean a forced reconnect every thirty seconds, for good.
     */
    private fun probeRound(socksEndpoint: SocksEndpoint): Boolean {
        val (first, second) = ProbeTargets.secondOpinionHosts()
        return probe(socksEndpoint, first) || probe(socksEndpoint, second)
    }

    private fun probe(socksEndpoint: SocksEndpoint, host: String): Boolean = try {
        Socket().use { socket ->
            socket.soTimeout = PROBE_TIMEOUT_MS.toInt()
            socket.connect(InetSocketAddress(socksEndpoint.host, socksEndpoint.port), PROBE_TIMEOUT_MS.toInt())
            Socks5Client.connect(socket, socksEndpoint.username, socksEndpoint.password, host, ProbeTargets.PORT)
        }
        true
    } catch (error: IOException) {
        false
    } catch (error: IllegalStateException) {
        false
    }

    private companion object {
        const val PROBE_INTERVAL_MS = 15_000L
        const val PROBE_JITTER_MS = 5_000L
        const val PROBE_TIMEOUT_MS = 6_000L
        const val CONSECUTIVE_FAILURES_THRESHOLD = 2
    }
}
