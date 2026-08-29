package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.di.IoDispatcher
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayLogLevel
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.XrayTrafficStats
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import libv2ray.CoreCallbackHandler
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
internal class XrayTelemetryMonitor @Inject constructor(
    private val statsSource: XrayStatsSource,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : XrayMonitoring, CoreCallbackHandler {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val _state = MutableStateFlow(XrayEngineState.IDLE)

    private val _stats = MutableSharedFlow<XrayTrafficStats>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _logs = MutableSharedFlow<XrayLogMessage>(
        extraBufferCapacity = LOG_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _healthDegraded = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val state: StateFlow<XrayEngineState> = _state.asStateFlow()
    override val stats: Flow<XrayTrafficStats> = _stats.asSharedFlow()
    override val logs: Flow<XrayLogMessage> = _logs.asSharedFlow()
    override val healthDegraded: Flow<Unit> = _healthDegraded.asSharedFlow()

    private val lifecycle = Mutex()
    private var statsJob: Job? = null
    private var logcatReader: GoLogcatReader? = null

    private val logSequence = AtomicLong()

    private var uplinkTotalBytes = 0L
    private var downlinkTotalBytes = 0L
    private var pollIntervalMillis = MIN_STATS_INTERVAL_MS

    suspend fun starting() = lifecycle.withLock {
        _state.value = XrayEngineState.STARTING
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun running(intervalMillis: Long) = lifecycle.withLock {
        check(statsJob == null) { "Telemetry is already tracking a running Xray session." }
        uplinkTotalBytes = 0L
        downlinkTotalBytes = 0L
        _stats.resetReplayCache()
        pollIntervalMillis = intervalMillis.coerceAtLeast(MIN_STATS_INTERVAL_MS)
        _state.value = XrayEngineState.RUNNING

        logcatReader = GoLogcatReader(scope) { line -> _logs.tryEmit(line.toLogMessage()) }
            .also { it.start() }

        statsJob = scope.launch {
            while (isActive) {
                delay(pollIntervalMillis.milliseconds)
                statsSource.queryAllOutboundTrafficStats()?.let { payload ->
                    _stats.tryEmit(payload.toTrafficStats())
                }
            }
        }
    }

    suspend fun stopping() {
        val (endingStats, endingLogcat) = lifecycle.withLock {
            _state.value = XrayEngineState.STOPPING
            (statsJob to logcatReader).also {
                statsJob = null
                logcatReader = null
            }
        }

        endingStats?.cancelAndJoin()
        endingLogcat?.stop()
        _state.value = XrayEngineState.STOPPED
    }

    fun degraded() {
        _healthDegraded.tryEmit(Unit)
    }

    fun failed(error: Throwable) {
        _state.value = XrayEngineState.ERROR
        _logs.tryEmit(
            XrayLogMessage(
                level = XrayLogLevel.ERROR,
                message = error.message ?: "Unable to start Xray",
                sequence = logSequence.getAndIncrement(),
            )
        )
    }

    override fun startup(): Long {
        Timber.i("XrayTelemetryMonitor: core startup")
        return 0
    }

    override fun shutdown(): Long {
        Timber.i("XrayTelemetryMonitor: core shutdown")
        return 0
    }

    override fun onEmitStatus(code: Long, message: String?): Long {
        Timber.i("XrayTelemetryMonitor: core status $code: $message")
        return 0
    }

    private fun String.toTrafficStats(): XrayTrafficStats {
        var uplinkDelta = 0L
        var downlinkDelta = 0L
        split(';').forEach { entry ->
            if (entry.isBlank()) return@forEach
            val parts = entry.split(',', limit = 3)
            if (parts.size != 3) return@forEach
            val value = parts[2].toLongOrNull() ?: return@forEach
            when (parts[1]) {
                "uplink" -> uplinkDelta += value
                "downlink" -> downlinkDelta += value
            }
        }

        uplinkTotalBytes += uplinkDelta
        downlinkTotalBytes += downlinkDelta
        val elapsedSeconds = pollIntervalMillis / 1000.0

        return XrayTrafficStats(
            uplinkBytesPerSecond = (uplinkDelta / elapsedSeconds).toLong().coerceAtLeast(0),
            downlinkBytesPerSecond = (downlinkDelta / elapsedSeconds).toLong().coerceAtLeast(0),
            uplinkTotalBytes = uplinkTotalBytes,
            downlinkTotalBytes = downlinkTotalBytes,
            activeConnectionsIn = 0,
            activeConnectionsOut = 0,
        )
    }

    private fun String.toLogMessage(): XrayLogMessage {
        val level = LOG_LEVEL_TAGS.entries
            .firstOrNull { (tag, _) -> contains(tag, ignoreCase = true) }
            ?.value
            ?: XrayLogLevel.INFO
        return XrayLogMessage(level, this, sequence = logSequence.getAndIncrement())
    }

    private companion object {
        const val LOG_BUFFER_CAPACITY = 64
        const val MIN_STATS_INTERVAL_MS = 250L

        val LOG_LEVEL_TAGS = mapOf(
            "[Debug]" to XrayLogLevel.DEBUG,
            "[Info]" to XrayLogLevel.INFO,
            "[Warning]" to XrayLogLevel.WARNING,
            "[Error]" to XrayLogLevel.ERROR,
        )
    }
}
