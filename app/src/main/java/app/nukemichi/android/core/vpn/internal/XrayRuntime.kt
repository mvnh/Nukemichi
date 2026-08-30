package app.nukemichi.android.core.vpn.internal

import android.content.Context
import app.nukemichi.android.core.di.IoDispatcher
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.core.vpn.XrayStatsSource
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class XrayRuntime @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : XrayStatsSource {
    private val mutex = Mutex()
    private var controller: CoreController? = null
    private val detachedScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Set by stopWithoutWaiting(), cleared once start() has waited on it (or given up). Lets a
    // fast disconnect-then-reconnect avoid racing the old CoreController for its inbound port,
    // without going back to blocking every plain disconnect on the native stopLoop() call.
    private var pendingStop: Job? = null

    init {
        Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")
    }

    suspend fun start(config: XrayRuntimeConfig, callbackHandler: CoreCallbackHandler) =
        mutex.withLock {
            check(controller == null) { "Xray is already running" }
            awaitPendingStop()
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
     * Fire-and-forget disconnect: xray-core's native stopLoop() can wedge for minutes on a stuck
     * goroutine (the same failure mode XrayHealthWatchdog watches for elsewhere), and skips the
     * mutex too, so a wedged stopLoop() can't block a later start(). Safe for a caller that
     * doesn't need xray actually stopped when this returns — start() still waits (briefly) for
     * this to finish before handing out a new CoreController, so a quick reconnect can't race the
     * old one for the same inbound port.
     */
    fun stopWithoutWaiting() {
        val running = controller ?: return
        controller = null
        pendingStop = detachedScope.launch {
            runCatching { running.stopLoop() }
        }
    }

    // Bounded, not unconditional: a stuck stopLoop() must not turn a plain reconnect into the
    // same kind of indefinite wait stopWithoutWaiting() exists to avoid. If it's still not done
    // by the timeout, start() proceeds anyway — worst case a bind conflict on the old inbound
    // port, which surfaces as a normal start failure rather than a hang.
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
