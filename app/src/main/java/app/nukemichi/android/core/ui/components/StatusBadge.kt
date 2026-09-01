package app.nukemichi.android.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString

@Composable
fun StatusBadge(
    text: UiText,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val dimens = MaterialTheme.dimens

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text.asString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = dimens.s, vertical = dimens.xs)
        )
    }
}
