package app.nukemichi.android.core.vpn.internal

import android.content.Context
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.core.vpn.XrayStatsSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class XrayRuntime @Inject constructor(
    @ApplicationContext context: Context,
) : XrayStatsSource {
    private val mutex = Mutex()
    private var controller: CoreController? = null

    init {
        Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")
    }

    suspend fun start(config: XrayRuntimeConfig, callbackHandler: CoreCallbackHandler) =
        mutex.withLock {
            check(controller == null) { "Xray is already running" }
            val running = CoreController(callbackHandler)
            running.startLoop(config.rawJson, 0)
            check(running.isRunning) { "Xray core reported success but is not running." }
            controller = running
        }

    suspend fun stop() = mutex.withLock {
        controller?.let { running -> runCatching { running.stopLoop() } }
        controller = null
    }

    override fun queryAllOutboundTrafficStats(): String? = controller?.queryAllOutboundTrafficStats()
}
