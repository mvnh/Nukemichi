package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.model.ServerUi
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens

@Composable
internal fun ServerRow(
    server: ServerUi,
    shape: Shape,
    onClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    val colors = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (server.isSelected) colors.primaryContainer else colors.surfaceContainerLow,
        animationSpec = dashboardEffectsSpec(),
        label = "serverRowContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (server.isSelected) colors.onPrimaryContainer else colors.onSurface,
        animationSpec = dashboardEffectsSpec(),
        label = "serverRowContent",
    )
    // Neutral on a plain row; lifted to the surface on a selected one so it still reads as a separate circle.
    val flagContainerColor by animateColorAsState(
        targetValue = if (server.isSelected) colors.surface else colors.surfaceContainerHighest,
        animationSpec = dashboardEffectsSpec(),
        label = "serverRowFlagContainer",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { selected = server.isSelected },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = dimens.xxl + dimens.m)
                .padding(start = dimens.l, end = dimens.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.l),
        ) {
            ServerFlag(flag = server.flag, containerColor = flagContainerColor)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = server.stack,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onDetailsClick) {
                Icon(
                    imageVector = NukemichiIcons.Outlined.Info,
                    contentDescription = stringResource(R.string.dashboard_server_details_cd, server.name),
                )
            }
        }
    }
}
