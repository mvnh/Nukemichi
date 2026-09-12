package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens

/** The group's last segment: a named action, where a bare + in the header said nothing about what it adds. */
@Composable
internal fun AddServerRow(
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = dimens.xxl + dimens.m)
                .padding(horizontal = dimens.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.l),
        ) {
            // Same slot width as a server's flag, so the label lines up with the server names above it.
            Box(modifier = Modifier.size(dimens.control), contentAlignment = Alignment.Center) {
                Icon(imageVector = NukemichiIcons.Outlined.Add, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.dashboard_subscription_add_server),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
