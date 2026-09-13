package app.nukemichi.android.core.vpn.internal

/**
 * Seam around [android.os.SystemClock.elapsedRealtime], the same way [app.nukemichi.android.core.vpn.XrayStatsSource]
 * is a seam around xray-core's own stats query: the real implementation is a one-liner with a
 * single caller ([XrayTelemetryMonitor]), but that call is an unmockable Android framework method,
 * so plain JVM unit tests need something to substitute instead.
 */
internal fun interface ElapsedRealtimeSource {
    fun elapsedRealtimeMillis(): Long
}
