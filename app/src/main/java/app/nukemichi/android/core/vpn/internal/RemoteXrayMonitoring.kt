package app.nukemichi.android.core.vpn.internal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import app.nukemichi.android.core.di.MainDispatcher
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayLogLevel
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayTrafficStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteXrayMonitoring @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
) : XrayMonitoring {

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private val events: Flow<TelemetryEvent> = callbackFlow {
        val client = Messenger(
            Handler(Looper.getMainLooper()) { message ->
                message.toTelemetryEvent()?.let(::trySend)
                true
            }
        )

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Timber.i("RemoteXrayMonitoring: :vpn process (re)connected, registering")
                runCatching {
                    Messenger(binder).send(
                        Message.obtain(null, VpnIpcProtocol.MSG_REGISTER_CLIENT)
                            .apply { replyTo = client }
                    )
                }.onFailure { Timber.w(it, "Failed to register for VPN telemetry") }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Timber.w("RemoteXrayMonitoring: :vpn process disconnected")
            }
        }

        Timber.i("RemoteXrayMonitoring: binding to VpnIpcService")
        context.bindService(
            Intent(context, VpnIpcService::class.java),
            connection,
            Context.BIND_AUTO_CREATE,
        )

        awaitClose { runCatching { context.unbindService(connection) } }
    }.shareIn(scope, SharingStarted.WhileSubscribed(UNBIND_GRACE_MS), replay = 0)

    override val state: StateFlow<XrayEngineState> = events
        .filterIsInstance<TelemetryEvent.State>()
        .map { it.state }
        .stateIn(scope, SharingStarted.WhileSubscribed(UNBIND_GRACE_MS), XrayEngineState.IDLE)

    override val stats: Flow<XrayTrafficStats> = events
        .filterIsInstance<TelemetryEvent.Stats>()
        .map { it.stats }

    override val logs: Flow<XrayLogMessage> = events
        .filterIsInstance<TelemetryEvent.Log>()
        .map { it.log }
        .withDropMarkers()

    override val healthDegraded: Flow<Unit> = events
        .filterIsInstance<TelemetryEvent.HealthDegraded>()
        .map { }

    private fun Message.toTelemetryEvent(): TelemetryEvent? = when (what) {
        VpnIpcProtocol.MSG_STATE_CHANGED ->
            XrayEngineState.entries.getOrNull(arg1)?.let(TelemetryEvent::State)

        VpnIpcProtocol.MSG_STATS_UPDATED -> TelemetryEvent.Stats(data.toTrafficStats())
        VpnIpcProtocol.MSG_LOG_LINE -> TelemetryEvent.Log(data.toLogMessage())
        VpnIpcProtocol.MSG_HEALTH_DEGRADED -> TelemetryEvent.HealthDegraded
        else -> null
    }

    private fun Flow<XrayLogMessage>.withDropMarkers(): Flow<XrayLogMessage> = flow {
        var expected = -1L
        collect { log ->
            if (expected >= 0 && log.sequence > expected) {
                emit(
                    XrayLogMessage(
                        level = XrayLogLevel.WARNING,
                        message = "… ${log.sequence - expected} log line(s) dropped …",
                        sequence = expected,
                    )
                )
            }
            expected = log.sequence + 1
            emit(log)
        }
    }

    private companion object {
        const val UNBIND_GRACE_MS = 5_000L
    }
}
