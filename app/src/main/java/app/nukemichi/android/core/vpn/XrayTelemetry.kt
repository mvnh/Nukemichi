package app.nukemichi.android.core.vpn

enum class XrayEngineState {
    IDLE,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    ERROR
}

data class XrayTrafficStats(
    val uplinkBytesPerSecond: Long,
    val downlinkBytesPerSecond: Long,
    val uplinkTotalBytes: Long,
    val downlinkTotalBytes: Long,
    val activeConnectionsIn: Int,
    val activeConnectionsOut: Int
)

data class XrayLogMessage(
    val level: Int,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    /**
     * Monotonic counter assigned where the line is first read. Log delivery is lossy by design
     * (bounded buffers, a process boundary), so a gap between consecutive received values is how a
     * consumer knows lines went missing instead of silently rendering a hole.
     */
    val sequence: Long = 0,
)

object XrayLogLevel {
    const val DEBUG = 0
    const val INFO = 1
    const val WARNING = 2
    const val ERROR = 3
}
