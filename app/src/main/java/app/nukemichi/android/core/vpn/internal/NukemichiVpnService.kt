package app.nukemichi.android.core.vpn.internal

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import app.nukemichi.android.MainActivity
import app.nukemichi.android.R
import app.nukemichi.android.core.di.IoDispatcher
import app.nukemichi.android.core.vpn.XrayJson
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

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
        Timber.i("onStartCommand: action=%s startId=%d", intent?.action, startId)
        when (intent?.action) {
            ACTION_START, ACTION_RELOAD -> {
                if (!isStarting.compareAndSet(false, true)) {
                    Timber.w("Ignoring %s: a start is already in progress.", intent.action)
                } else {
                    val config = intent.configOrNull()
                    if (config == null) {
                        isStarting.set(false)
                        Timber.e("Ignoring %s: runtime config missing/undecodable from intent extras.", intent.action)
                        telemetry.failed(IllegalArgumentException("Xray runtime configuration is missing."))
                    } else {
                        startVpn(config)
                    }
                }
            }

            ACTION_STOP -> stopVpn()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Timber.i("onDestroy")
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startVpn(config: XrayRuntimeConfig) {
        Timber.i("startVpn: begin")
        lastConfig = config
        startForeground(NOTIFICATION_ID, createNotification())
        scope.launch {
            try {
                lifecycleMutex.withLock {
                    Timber.d("startVpn: tearing down any previous tunnel/session before starting fresh")
                    healthWatchdog.stop()
                    teardown()
                    telemetry.stopping()
                    telemetry.starting()
                    Timber.d("startVpn: establishing TUN")
                    val establishedTun = establishTun()
                    Timber.d("startVpn: TUN established, starting hev-socks5-tunnel")
                    try {
                        hevSocks5Tunnel.start(establishedTun, config.socksEndpoint)
                    } catch (error: Throwable) {
                        Timber.e(error, "startVpn: hev-socks5-tunnel failed to start")
                        establishedTun.close()
                        throw error
                    }
                    Timber.d("startVpn: hev-socks5-tunnel running, starting xray core")
                    try {
                        runtime.start(config, telemetry)
                    } catch (error: Throwable) {
                        Timber.e(error, "startVpn: xray core failed to start")
                        hevSocks5Tunnel.stop()
                        establishedTun.close()
                        throw error
                    }
                    tunInterface = establishedTun
                    telemetry.running(config.statusIntervalMillis)
                    healthWatchdog.start(scope, config.socksEndpoint, ::onHealthDegraded)
                    Timber.i("startVpn: complete, RUNNING")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                Timber.e(error, "startVpn: failed")
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
        // This process is about to die (see stopVpn's killProcess) and can't restart itself —
        // schedule the restart through AlarmManager, which survives that, instead of leaving it
        // to whichever app process happens to be alive to notice the degraded signal and reconnect.
        lastConfig?.let(::scheduleRestart)
        stopVpn()
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

    private fun stopVpn() {
        Timber.i("stopVpn: begin")
        scope.launch {
            lifecycleMutex.withLock {
                healthWatchdog.stop()
                teardown()
                telemetry.stopping()
                stopForeground(STOP_FOREGROUND_REMOVE)
                Timber.i("stopVpn: torn down, restarting :vpn process for a clean slate")
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
            // No IPv6 address/route on the interface — apps see no IPv6 path at all and go
            // straight to IPv4 instead of dialing out, timing out, then falling back. xray's own
            // routing still blackholes ::/0 from the socks inbound as defense in depth regardless.
            .addDnsServer("1.1.1.1")
            .addDisallowedApplication(packageName)
            .setMtu(VpnTunnelDefaults.VPN_MTU)
            .setSession("Nukemichi Xray")
            .establish()
    ) { "Android rejected VPN interface establishment." }

    private suspend fun teardown() {
        hevSocks5Tunnel.stop()
        runtime.stop()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_vpn_title),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
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
