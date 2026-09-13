package app.nukemichi.android.core.vpn.internal

import android.content.Context
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.platform.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

@Singleton
internal class XrayRuntime @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val geoAssetInstaller: GeoAssetInstaller,
) : XrayStatsSource {
    private val mutex = Mutex()

    // stopWithoutWaiting() writes this without the mutex while start()/stop() read it under one,
    // on different IO-pool threads. A stale read would answer start()'s "already running" check
    // from cache.
    @Volatile
    private var controller: CoreController? = null
    private val detachedScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Set by stopWithoutWaiting(), cleared once start() has waited on it. Lets a fast
    // disconnect-then-reconnect avoid racing the old CoreController for its inbound port.
    @Volatile
    private var pendingStop: Job? = null

    init {
        Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")
    }

    suspend fun start(config: XrayRuntimeConfig, callbackHandler: CoreCallbackHandler) =
        mutex.withLock {
            check(controller == null) { "Xray is already running" }
            awaitPendingStop()
            // Must land in filesDir before startLoop() below, since that's when Xray resolves the
            // routing config's geosite:/geoip: rules against whatever is staged there.
            geoAssetInstaller.ensureInstalled()
            val running = CoreController(callbackHandler)
            running.startLoop(config.rawJson, 0)
            check(running.isRunning) { "Xray core reported success but is not running." }
            controller = running
        }

    suspend fun stop() = mutex.withLock {
        controller?.let { running -> runCatching { running.stopLoop() } }
        controller = null
    }

    /**
     * Fire-and-forget disconnect, for callers that don't need xray actually stopped on return.
     * stopLoop() can wedge for minutes on a stuck goroutine, so this skips the mutex as well and a
     * wedged stop can never block a later start(). start() still waits briefly on the pending job,
     * so a quick reconnect cannot race the old controller for the same inbound port.
     */
    fun stopWithoutWaiting() {
        val running = controller ?: return
        controller = null
        pendingStop = detachedScope.launch {
            runCatching { running.stopLoop() }
        }
    }

    // Bounded, so a stuck stopLoop() cannot reintroduce the indefinite wait stopWithoutWaiting()
    // exists to avoid. On timeout start() proceeds anyway and a bind conflict on the old inbound
    // port surfaces as an ordinary start failure.
    private suspend fun awaitPendingStop() {
        val job = pendingStop ?: return
        withTimeoutOrNull(PENDING_STOP_TIMEOUT_MS) { job.join() }
        pendingStop = null
    }

    override fun queryAllOutboundTrafficStats(): String? = controller?.queryAllOutboundTrafficStats()

    private companion object {
        const val PENDING_STOP_TIMEOUT_MS = 1_500L
    }
}
