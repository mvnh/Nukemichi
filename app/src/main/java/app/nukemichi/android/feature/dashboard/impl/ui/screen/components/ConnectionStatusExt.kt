package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected
import app.nukemichi.android.platform.ui.util.UiText
import kotlinx.coroutines.delay

@Composable
internal fun DashboardContract.State.statusText(): UiText {
    val since = connectedSinceRealtime
    if (!isConnected || since == null) return engineState.label()
    val elapsed = rememberRealtimeNow(since) - since
    return UiText.Raw(stringResource(R.string.dashboard_connected_for, formatDuration(elapsed)))
}

@Composable
private fun rememberRealtimeNow(since: Long): Long {
    var now by remember(since) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(since) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    return now
}

private fun XrayEngineState.label(): UiText = when (this) {
    XrayEngineState.IDLE -> UiText.Resource(R.string.dashboard_state_not_connected)
    XrayEngineState.STARTING -> UiText.Resource(R.string.dashboard_state_connecting)
    XrayEngineState.RUNNING -> UiText.Resource(R.string.dashboard_state_connected)
    XrayEngineState.STOPPING -> UiText.Resource(R.string.dashboard_state_disconnecting)
    XrayEngineState.STOPPED -> UiText.Resource(R.string.dashboard_state_not_connected)
    XrayEngineState.ERROR -> UiText.Resource(R.string.dashboard_state_error)
}

@Composable
private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) stringResource(R.string.duration_hours_minutes, hours, minutes)
    else stringResource(R.string.duration_minutes_seconds, minutes, seconds)
}
