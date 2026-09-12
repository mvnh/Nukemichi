package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isBusy
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected
import app.nukemichi.android.platform.ui.components.StatusBadge
import app.nukemichi.android.platform.ui.theme.size.dimens
import kotlinx.coroutines.delay

private const val SLOW_TRANSITION_HINT_DELAY_MS = 5_000L

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

        StatsRow(
            stats = state.stats,
            visible = state.isConnected,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

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
