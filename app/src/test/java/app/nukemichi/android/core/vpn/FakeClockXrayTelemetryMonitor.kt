package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.internal.XrayTelemetryMonitor
import kotlinx.coroutines.CoroutineDispatcher

/**
 * [XrayTelemetryMonitor] with its one Android-framework seam ([XrayTelemetryMonitor.elapsedRealtimeMillis])
 * overridden, so these plain JVM unit tests don't hit `SystemClock.elapsedRealtime()`'s
 * "not mocked" `RuntimeException` (see https://developer.android.com/r/studio-ui/build/not-mocked).
 * The exact value doesn't matter to [XrayTelemetryMonitorTest] or [XrayTelemetryMonitorStatsTest] -
 * neither asserts on [XrayTelemetryMonitor.runningSinceRealtime] - only that reading it doesn't throw.
 */
internal class FakeClockXrayTelemetryMonitor(
    statsSource: XrayStatsSource,
    ioDispatcher: CoroutineDispatcher,
) : XrayTelemetryMonitor(statsSource, ioDispatcher) {
    override fun elapsedRealtimeMillis(): Long = FAKE_ELAPSED_REALTIME_MS

    companion object {
        const val FAKE_ELAPSED_REALTIME_MS = 1_000_000L
    }
}
