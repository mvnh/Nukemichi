package app.nukemichi.android.core.vpn.internal

import android.os.Bundle
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayTrafficStats

internal fun XrayTrafficStats.toBundle(): Bundle = Bundle().apply {
    putLong(VpnIpcProtocol.KEY_UPLINK_BPS, uplinkBytesPerSecond)
    putLong(VpnIpcProtocol.KEY_DOWNLINK_BPS, downlinkBytesPerSecond)
    putLong(VpnIpcProtocol.KEY_UPLINK_TOTAL, uplinkTotalBytes)
    putLong(VpnIpcProtocol.KEY_DOWNLINK_TOTAL, downlinkTotalBytes)
    putInt(VpnIpcProtocol.KEY_CONN_IN, activeConnectionsIn)
    putInt(VpnIpcProtocol.KEY_CONN_OUT, activeConnectionsOut)
}

internal fun Bundle.toTrafficStats(): XrayTrafficStats = XrayTrafficStats(
    uplinkBytesPerSecond = getLong(VpnIpcProtocol.KEY_UPLINK_BPS),
    downlinkBytesPerSecond = getLong(VpnIpcProtocol.KEY_DOWNLINK_BPS),
    uplinkTotalBytes = getLong(VpnIpcProtocol.KEY_UPLINK_TOTAL),
    downlinkTotalBytes = getLong(VpnIpcProtocol.KEY_DOWNLINK_TOTAL),
    activeConnectionsIn = getInt(VpnIpcProtocol.KEY_CONN_IN),
    activeConnectionsOut = getInt(VpnIpcProtocol.KEY_CONN_OUT),
)

internal fun XrayLogMessage.toBundle(): Bundle = Bundle().apply {
    putInt(VpnIpcProtocol.KEY_LOG_LEVEL, level)
    putString(VpnIpcProtocol.KEY_LOG_MESSAGE, message)
    putLong(VpnIpcProtocol.KEY_LOG_TIMESTAMP, timestampMillis)
    putLong(VpnIpcProtocol.KEY_LOG_SEQUENCE, sequence)
}

internal fun Bundle.toLogMessage(): XrayLogMessage = XrayLogMessage(
    level = getInt(VpnIpcProtocol.KEY_LOG_LEVEL),
    message = getString(VpnIpcProtocol.KEY_LOG_MESSAGE).orEmpty(),
    timestampMillis = getLong(VpnIpcProtocol.KEY_LOG_TIMESTAMP),
    sequence = getLong(VpnIpcProtocol.KEY_LOG_SEQUENCE),
)
