package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.nukemichi.android.core.ui.components.StatusBadge
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.core.ui.util.dashedBorder

@Composable
internal fun HelloActionCard(
    title: UiText,
    description: UiText,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badgeText: UiText? = null,
) {
    val dimens = MaterialTheme.dimens
    val shape = CardDefaults.shape
    val dashedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Card(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .then(if (!enabled) Modifier.dashedBorder(color = dashedColor, shape = shape) else Modifier)
            .alpha(if (enabled) 1f else 0.5f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, disabledElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.m),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.icon)
                )

                Text(
                    text = title.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (badgeText != null) {
                    StatusBadge(text = badgeText)
                }
            }

            Spacer(modifier = Modifier.height(dimens.s))

            Text(
                text = description.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
