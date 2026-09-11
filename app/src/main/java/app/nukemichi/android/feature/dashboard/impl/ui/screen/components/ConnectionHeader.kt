package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isBusy
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected
import app.nukemichi.android.platform.ui.components.StatusBadge
import app.nukemichi.android.platform.ui.theme.size.dimens
import app.nukemichi.android.platform.ui.util.UiText
import kotlinx.coroutines.delay

private const val SLOW_TRANSITION_HINT_DELAY_MS = 5_000L

/**
 * The top of the server list: the connection toggle, its status and, while connected, the traffic stats.
 * Whatever comes and goes here changes the header's real height, so the list below slides rather than jumps.
 */
@Composable
internal fun ConnectionHeader(
    state: DashboardContract.State,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // rememberIsConnectionToggleScrolledAway() measures against this padding and the toggle's size.
        Box(modifier = Modifier.padding(top = dimens.xl, bottom = dimens.xl)) {
            ConnectionToggle(state = state, onClick = onToggleConnection)
        }

        StatusBadge(
            text = state.statusText(),
            modifier = Modifier.animateContentSize(animationSpec = dashboardSpatialSpec()),
        )

        SlowTransitionHint(state = state)

        AnimatedVisibility(
            visible = state.isConnected,
            enter = expandVertically(animationSpec = dashboardSpatialSpec(), expandFrom = Alignment.Top) +
                fadeIn(animationSpec = dashboardEffectsSpec()),
            exit = shrinkVertically(animationSpec = dashboardSpatialSpec(), shrinkTowards = Alignment.Top) +
                fadeOut(animationSpec = dashboardEffectsSpec()),
        ) {
            StatsRow(
                stats = state.stats,
                modifier = Modifier
                    .padding(top = dimens.xl)
                    .fillMaxWidth(),
            )
        }
    }
}

/** True once the list has scrolled the connection toggle fully out of view, which is when the FAB takes over. */
@Composable
internal fun rememberIsConnectionToggleScrolledAway(listState: LazyListState): State<Boolean> {
    val dimens = MaterialTheme.dimens
    val toggleBottomPx = with(LocalDensity.current) { (dimens.xl + dimens.xxl + dimens.xxl).roundToPx() }
    return remember(listState, toggleBottomPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > toggleBottomPx
        }
    }
}

@Composable
private fun ColumnScope.SlowTransitionHint(state: DashboardContract.State) {
    val dimens = MaterialTheme.dimens
    // Reset on every engine state change, so the hint folds away as soon as the transition it describes ends.
    var isSlow by remember(state.engineState) { mutableStateOf(false) }
    LaunchedEffect(state.engineState) {
        if (state.isBusy) {
            delay(SLOW_TRANSITION_HINT_DELAY_MS)
            isSlow = true
        }
    }

    AnimatedVisibility(
        visible = state.isBusy && isSlow,
        enter = expandVertically(animationSpec = dashboardSpatialSpec(), expandFrom = Alignment.Top) +
            fadeIn(animationSpec = dashboardEffectsSpec()),
        exit = shrinkVertically(animationSpec = dashboardSpatialSpec(), shrinkTowards = Alignment.Top) +
            fadeOut(animationSpec = dashboardEffectsSpec()),
    ) {
        Text(
            text = stringResource(R.string.dashboard_state_taking_a_while),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimens.s),
        )
    }
}

// Connected folds the timer into the badge itself: "Connected for 1h 25m" rather than a status and a separate timer.
@Composable
private fun DashboardContract.State.statusText(): UiText {
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
