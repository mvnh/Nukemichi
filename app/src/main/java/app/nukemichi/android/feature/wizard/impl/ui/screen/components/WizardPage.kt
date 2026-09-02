package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.nukemichi.android.core.ui.theme.size.dimens

private const val SCROLLBAR_TRACK_ALPHA = 0.06f
private const val SCROLLBAR_THUMB_ALPHA = 0.24f
private const val SCROLLBAR_MIN_THUMB_FRACTION = 0.08f
private val SCROLLBAR_WIDTH = 4.dp

@Composable
internal fun WizardPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val dimens = MaterialTheme.dimens
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = SCROLLBAR_TRACK_ALPHA)
    val thumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = SCROLLBAR_THUMB_ALPHA)
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportPx = with(density) { maxHeight.toPx() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(dimens.l),
            content = content
        )

        // A plain, non-interactive scroll-position indicator instead of an edge shadow. A fade
        // only reads as "content is clipped here" when there's a visible container frame to
        // anchor it to, which the page no longer has. This sits to the side and never overlaps
        // content, so unlike a floating hint icon it needs no backing surface of its own.
        if (scrollState.maxValue > 0) {
            val contentPx = viewportPx + scrollState.maxValue
            val thumbFraction = (viewportPx / contentPx).coerceIn(SCROLLBAR_MIN_THUMB_FRACTION, 1f)
            val positionFraction = scrollState.value / scrollState.maxValue.toFloat()

            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = dimens.s)
                    .width(SCROLLBAR_WIDTH)
            ) {
                val cornerRadius = CornerRadius(size.width / 2)
                val thumbHeight = size.height * thumbFraction
                val thumbOffsetY = (size.height - thumbHeight) * positionFraction

                drawRoundRect(color = trackColor, cornerRadius = cornerRadius)
                drawRoundRect(
                    color = thumbColor,
                    topLeft = Offset(0f, thumbOffsetY),
                    size = Size(size.width, thumbHeight),
                    cornerRadius = cornerRadius,
                )
            }
        }
    }
}
