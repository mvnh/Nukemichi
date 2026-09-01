package app.nukemichi.android.core.vpn.internal

internal object VpnIpcProtocol {
    const val MSG_REGISTER_CLIENT = 1
    const val MSG_UNREGISTER_CLIENT = 2
    const val MSG_STATE_CHANGED = 10
    const val MSG_STATS_UPDATED = 11
    const val MSG_LOG_LINE = 12
    /** No payload. See [app.nukemichi.android.core.vpn.internal.XrayHealthWatchdog]. */
    const val MSG_HEALTH_DEGRADED = 13

    const val KEY_UPLINK_BPS = "uplink_bps"
    const val KEY_DOWNLINK_BPS = "downlink_bps"
    const val KEY_UPLINK_TOTAL = "uplink_total"
    const val KEY_DOWNLINK_TOTAL = "downlink_total"
    const val KEY_CONN_IN = "conn_in"
    const val KEY_CONN_OUT = "conn_out"

    const val KEY_LOG_LEVEL = "log_level"
    const val KEY_LOG_MESSAGE = "log_message"
    const val KEY_LOG_TIMESTAMP = "log_timestamp"
    const val KEY_LOG_SEQUENCE = "log_sequence"
}
