package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Material 3's extra-large and small corner tokens: the outer edges of a connected run, and where two rows meet.
// Plain Dp rather than MaterialTheme.shapes so the header can animate between them.
internal val SegmentOuterCorner = 28.dp
internal val SegmentInnerCorner = 8.dp

/** Corners for row [index] of a connected run of [count] rows. */
internal fun segmentShape(index: Int, count: Int): Shape {
    val top = if (index == 0) SegmentOuterCorner else SegmentInnerCorner
    val bottom = if (index == count - 1) SegmentOuterCorner else SegmentInnerCorner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}
