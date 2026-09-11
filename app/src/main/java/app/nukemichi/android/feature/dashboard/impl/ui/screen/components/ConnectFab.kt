package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isBusy
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens

/** Stands in for [ConnectionToggle] while that is scrolled out of view: same action, icons and busy guard. */
@Composable
internal fun ConnectFab(
    state: DashboardContract.State,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    // Primary while connected, like the toggle. Otherwise the regular FAB container: the toggle's
    // surfaceContainerHigh would barely separate from the list rows scrolling underneath.
    val containerColor = if (state.isConnected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    ExtendedFloatingActionButton(
        onClick = { if (!state.isBusy) onClick() },
        // "Connect" and "Disconnect" differ in width; ease between them instead of snapping.
        modifier = modifier.animateContentSize(animationSpec = dashboardSpatialSpec()),
        containerColor = containerColor,
        contentColor = contentColorFor(containerColor),
        icon = {
            if (state.isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimens.icon),
                    color = LocalContentColor.current,
                    strokeWidth = dimens.xs,
                )
            } else {
                Icon(
                    imageVector = if (state.isConnected) NukemichiIcons.Outlined.Shield else NukemichiIcons.Outlined.Cable,
                    contentDescription = null,
                )
            }
        },
        text = {
            Text(text = stringResource(if (state.isConnected) R.string.dashboard_disconnect else R.string.dashboard_connect))
        },
    )
}
