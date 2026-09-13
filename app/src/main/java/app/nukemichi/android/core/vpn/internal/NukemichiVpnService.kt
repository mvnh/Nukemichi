package app.nukemichi.android.core.vpn.internal

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import app.nukemichi.android.MainActivity
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.XrayJson
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import app.nukemichi.android.platform.di.IoDispatcher
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@AndroidEntryPoint
internal class NukemichiVpnService : VpnService() {

    @Inject
    internal lateinit var runtime: XrayRuntime

    @Inject
    internal lateinit var telemetry: XrayTelemetryMonitor

    @Inject
    internal lateinit var healthWatchdog: XrayHealthWatchdog

    @Inject
    internal lateinit var hevSocks5Tunnel: HevSocks5Tunnel

    @Inject
    @IoDispatcher
    internal lateinit var ioDispatcher: CoroutineDispatcher

    private val lifecycleMutex = Mutex()
    private var tunInterface: ParcelFileDescriptor? = null
    private lateinit var scope: CoroutineScope

    private val isStarting = AtomicBoolean(false)
    @Volatile
    private var lastConfig: XrayRuntimeConfig? = null

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(SupervisorJob() + ioDispatcher)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand: %s", intent?.action)
        when (intent?.action) {
            ACTION_START, ACTION_RELOAD -> {
                // First and unconditional: these arrive via startForegroundService, and any path
                // out of this branch that skips startForeground ends in
                // ForegroundServiceDidNotStartInTimeException. An undecodable config is one such
                // path, which a restart PendingIntent older than XrayRuntimeConfig's shape produces.
                startForeground(NOTIFICATION_ID, createNotification())
                if (!isStarting.compareAndSet(false, true)) {
                    Timber.w("Ignoring %s: a start is already in progress.", intent.action)
                } else {
                    val config = intent.configOrNull()
                    if (config == null) {
                        isStarting.set(false)
                        Timber.e("Ignoring %s: runtime config missing/undecodable from intent extras.", intent.action)
                        telemetry.failed(IllegalArgumentException("Xray runtime configuration is missing."))
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        startVpn(config)
                    }
                }
            }

            ACTION_STOP -> stopVpn()
        }
        return START_NOT_STICKY
    }

    /**
     * The user turned the VPN off from system settings, or another app took the tunnel over.
     * Without this the tun fd is already dead while hev-socks5-tunnel keeps pumping into it,
     * xray-core keeps running and the notification still claims a live connection.
     *
     * Deliberately does not call super: the default implementation stopSelf()s immediately, which
     * would run onDestroy and cancel the scope out from under the teardown below. stopVpn ends in
     * its own stopSelf once there is actually nothing left to stop.
     */
    override fun onRevoke() {
        Timber.w("onRevoke: VPN permission withdrawn, tearing the tunnel down")
        stopVpn()
    }

    override fun onDestroy() {
        Timber.i("onDestroy")
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startVpn(config: XrayRuntimeConfig) {
        lastConfig = config
        scope.launch {
            try {
                lifecycleMutex.withLock {
                    healthWatchdog.stop()
                    teardown()
                    telemetry.stopping()
                    telemetry.starting()
                    val establishedTun = establishTun()
                    try {
                        hevSocks5Tunnel.start(establishedTun, config.socksEndpoint)
                    } catch (error: Throwable) {
                        establishedTun.close()
                        throw error
                    }
                    try {
                        runtime.start(config, telemetry)
                    } catch (error: Throwable) {
                        hevSocks5Tunnel.stop()
                        establishedTun.close()
                        throw error
                    }
                    tunInterface = establishedTun
                    telemetry.running(config.statusIntervalMillis, config.serverId)
                    healthWatchdog.start(scope, config.socksEndpoint, ::onHealthDegraded)
                    Timber.i("VPN started")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                Timber.e(error, "VPN failed to start")
                telemetry.failed(error)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } finally {
                isStarting.set(false)
            }
        }
    }

    private suspend fun onHealthDegraded() {
        Timber.w("onHealthDegraded: tunnel silently stuck, forcing a full reconnect")
        telemetry.degraded()
        // This process is about to be killed and cannot restart itself, so the restart is handed
        // to AlarmManager, which outlives it.
        lastConfig?.let(::scheduleRestart)
        stopVpnAndRestartProcess()
    }

    private fun scheduleRestart(config: XrayRuntimeConfig) {
        val pendingIntent = PendingIntent.getService(
            this,
            RESTART_REQUEST_CODE,
            startIntent(this, config),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
            pendingIntent,
        )
    }

    // Plain user-initiated disconnect: tear down but keep this process alive. VpnIpcService is
    // bound from the main process, and killing a process out from under an active bind reads to
    // Android as a crash, earning an escalating restart backoff. Waiting out that backoff before
    // the UI could rebind is what made disconnects look like they took minutes.
    private fun stopVpn() {
        scope.launch {
            lifecycleMutex.withLock {
                healthWatchdog.stop()
                teardown(waitForXrayStop = false)
                telemetry.stopping()
                stopForeground(STOP_FOREGROUND_REMOVE)
                Timber.i("VPN stopped")
                stopSelf()
            }
        }
    }

    // Only for onHealthDegraded: xray-core may be wedged past what a fresh CoreController can fix
    // in-process, so this path pays the process kill, and its restart backoff, for a clean slate.
    private fun stopVpnAndRestartProcess() {
        scope.launch {
            lifecycleMutex.withLock {
                healthWatchdog.stop()
                teardown(waitForXrayStop = false)
                telemetry.stopping()
                stopForeground(STOP_FOREGROUND_REMOVE)
                Timber.i("VPN stopped; restarting the :vpn process for a clean slate")
                Process.killProcess(Process.myPid())
            }
        }
    }

    private fun Intent.configOrNull(): XrayRuntimeConfig? =
        getStringExtra(EXTRA_RUNTIME_CONFIG)?.let { payload ->
            runCatching { XrayJson.default.decodeFromString<XrayRuntimeConfig>(payload) }.getOrNull()
        }

    private fun establishTun(): ParcelFileDescriptor = checkNotNull(
        Builder()
            .addAddress(VpnTunnelDefaults.VPN_ADDRESS, VpnTunnelDefaults.VPN_PREFIX_LENGTH)
            .addRoute("0.0.0.0", 0)
            // No IPv6 address or route on the interface, so apps see no IPv6 path at all and go
            // straight to IPv4 instead of dialing out, timing out, then falling back. xray's own
            // routing still blackholes ::/0 from the socks inbound as defense in depth regardless.
            .addDnsServer("1.1.1.1")
            .addDisallowedApplication(packageName)
            .setMtu(VpnTunnelDefaults.VPN_MTU)
            .setSession("Nukemichi Xray")
            .establish()
    ) { "Android rejected VPN interface establishment." }

    // waitForXrayStop=false only on the disconnect path: stopLoop() can hang for minutes, and the
    // killProcess() right after frees the native state either way. startVpn's pre-flight teardown
    // keeps the process alive, so there the old instance really has to be gone first.
    private suspend fun teardown(waitForXrayStop: Boolean = true) {
        hevSocks5Tunnel.stop()
        if (waitForXrayStop) runtime.stop() else runtime.stopWithoutWaiting()
        tunInterface?.close()
        tunInterface = null
    }

    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            CONTENT_REQUEST_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val disconnectIntent = PendingIntent.getService(
            this,
            DISCONNECT_REQUEST_CODE,
            stopIntent(this),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(getString(R.string.notification_vpn_title))
            .setContentText(getString(R.string.notification_vpn_content))
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_launcher_monochrome, getString(R.string.notification_vpn_disconnect), disconnectIntent)
            .setOngoing(true)
            .build()
    }

    private fun ensureNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_vpn_title),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "app.nukemichi.xray.START"
        const val ACTION_RELOAD = "app.nukemichi.xray.RELOAD"
        const val ACTION_STOP = "app.nukemichi.xray.STOP"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "nukemichi_vpn"
        private const val EXTRA_RUNTIME_CONFIG = "runtime_config"
        private const val RESTART_REQUEST_CODE = 2001
        private const val RESTART_DELAY_MS = 3_000L
        private const val CONTENT_REQUEST_CODE = 2002
        private const val DISCONNECT_REQUEST_CODE = 2003

        fun startIntent(context: Context, config: XrayRuntimeConfig): Intent =
            configIntent(context, ACTION_START, config)

        fun reloadIntent(context: Context, config: XrayRuntimeConfig): Intent =
            configIntent(context, ACTION_RELOAD, config)

        fun stopIntent(context: Context): Intent = Intent(context, NukemichiVpnService::class.java)
            .setAction(ACTION_STOP)

        private fun configIntent(
            context: Context,
            action: String,
            config: XrayRuntimeConfig,
        ): Intent = Intent(context, NukemichiVpnService::class.java)
            .setAction(action)
            .putExtra(EXTRA_RUNTIME_CONFIG, XrayJson.default.encodeToString(config))
    }
}
