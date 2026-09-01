package app.nukemichi.android.core.vpn.internal

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import app.nukemichi.android.core.di.MainDispatcher
import app.nukemichi.android.core.vpn.XrayTrafficStats
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

@AndroidEntryPoint
internal class VpnIpcService : Service() {

    @Inject
    internal lateinit var telemetry: XrayTelemetryMonitor

    @Inject
    @MainDispatcher
    internal lateinit var mainDispatcher: CoroutineDispatcher

    // Registered from the binder thread's handler, iterated from the telemetry scope. Copy-on-write
    // rather than a lock because the read path runs on every log line while writes happen only when
    // a client binds or goes away.
    private val clients = CopyOnWriteArraySet<Messenger>()
    private val messenger = Messenger(IncomingHandler())

    @Volatile
    private var lastStats: XrayTrafficStats? = null

    private lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Timber.i("VpnIpcService: created in pid=%d", Process.myPid())
        scope = CoroutineScope(SupervisorJob() + mainDispatcher)

        telemetry.state.onEach { state ->
            broadcast(Message.obtain(null, VpnIpcProtocol.MSG_STATE_CHANGED, state.ordinal, 0))
        }.launchIn(scope)
        telemetry.stats.onEach { stats ->
            lastStats = stats
            broadcast(
                Message.obtain(null, VpnIpcProtocol.MSG_STATS_UPDATED)
                    .apply { data = stats.toBundle() }
            )
        }.launchIn(scope)
        telemetry.logs.onEach { log ->
            broadcast(
                Message.obtain(null, VpnIpcProtocol.MSG_LOG_LINE).apply { data = log.toBundle() }
            )
        }.launchIn(scope)
        telemetry.healthDegraded.onEach {
            broadcast(Message.obtain(null, VpnIpcProtocol.MSG_HEALTH_DEGRADED))
        }.launchIn(scope)
    }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    override fun onDestroy() {
        scope.cancel()
        clients.clear()
        super.onDestroy()
    }

    private fun broadcast(template: Message) {
        clients.forEach { client -> client.trySend(Message.obtain(template)) }
    }

    /**
     * Brings a newly-registered client up to date. Telemetry is broadcast on change, so without
     * this a client binding into an already-running connection would sit on IDLE and no traffic
     * until something happened to change, which on an idle-but-connected tunnel may be a while.
     */
    private fun replayCurrentState(client: Messenger) {
        client.trySend(
            Message.obtain(
                null,
                VpnIpcProtocol.MSG_STATE_CHANGED,
                telemetry.state.value.ordinal,
                0
            )
        )
        lastStats?.let { stats ->
            client.trySend(
                Message.obtain(null, VpnIpcProtocol.MSG_STATS_UPDATED)
                    .apply { data = stats.toBundle() }
            )
        }
    }

    private fun Messenger.trySend(message: Message) {
        try {
            send(message)
        } catch (error: RemoteException) {
            clients.remove(this)
        }
    }

    private inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                VpnIpcProtocol.MSG_REGISTER_CLIENT -> msg.replyTo?.let { client ->
                    Timber.i("VpnIpcService: client registered, state=%s", telemetry.state.value)
                    clients.add(client)
                    replayCurrentState(client)
                }

                VpnIpcProtocol.MSG_UNREGISTER_CLIENT -> msg.replyTo?.let(clients::remove)
                else -> super.handleMessage(msg)
            }
        }
    }
}
