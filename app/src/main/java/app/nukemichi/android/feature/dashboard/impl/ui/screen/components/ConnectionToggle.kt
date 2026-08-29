package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isBusy
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.isConnected

@Composable
internal fun ConnectionToggle(
    state: DashboardContract.State,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    val containerColor = when {
        state.isConnected -> MaterialTheme.colorScheme.primary
        state.engineState == XrayEngineState.ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconColor = if (state.isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .size(dimens.xxl + dimens.xxl)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(enabled = !state.isBusy, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isBusy) {
            CircularProgressIndicator(color = iconColor)
        } else {
            Icon(
                imageVector = if (state.isConnected) NukemichiIcons.Common.Shield else NukemichiIcons.Common.Cable,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(dimens.xxl),
            )
        }
    }
}
