package app.nukemichi.android.core.vpn.internal

/**
 * Seam around [android.os.SystemClock.elapsedRealtime]. The implementation is a one-liner, but the
 * call is an unmockable framework method, so JVM unit tests for [XrayTelemetryMonitor] need
 * something to substitute.
 */
internal fun interface ElapsedRealtimeSource {
    fun elapsedRealtimeMillis(): Long
}
