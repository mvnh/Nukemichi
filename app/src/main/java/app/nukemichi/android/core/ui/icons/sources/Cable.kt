package app.nukemichi.android.core.ui.icons.sources

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val _cable: ImageVector
    get() {
        if (__cable != null) {
            return __cable!!
        }
        __cable =
            ImageVector.Builder(
                name = "cable",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(5f, 21f)
                        quadTo(4.58f, 21f, 4.29f, 20.71f)
                        quadTo(4f, 20.43f, 4f, 20f)
                        verticalLineTo(19f)
                        horizontalLineTo(3f)
                        verticalLineTo(15f)
                        quadTo(3f, 14.58f, 3.29f, 14.29f)
                        reflectiveQuadTo(4f, 14f)
                        horizontalLineTo(5f)
                        verticalLineTo(7f)
                        quadTo(5f, 5.35f, 6.18f, 4.17f)
                        reflectiveQuadTo(9f, 3f)
                        reflectiveQuadToRelative(2.83f, 1.17f)
                        reflectiveQuadTo(13f, 7f)
                        verticalLineTo(17f)
                        quadToRelative(0f, 0.82f, 0.59f, 1.41f)
                        reflectiveQuadTo(15f, 19f)
                        reflectiveQuadToRelative(1.41f, -0.59f)
                        reflectiveQuadTo(17f, 17f)
                        verticalLineTo(10f)
                        horizontalLineTo(16f)
                        quadTo(15.58f, 10f, 15.29f, 9.71f)
                        reflectiveQuadTo(15f, 9f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(1f)
                        verticalLineTo(4f)
                        quadTo(16f, 3.57f, 16.29f, 3.29f)
                        reflectiveQuadTo(17f, 3f)
                        horizontalLineToRelative(2f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 4f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(1f)
                        verticalLineTo(9f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(20f, 10f)
                        horizontalLineTo(19f)
                        verticalLineToRelative(7f)
                        quadToRelative(0f, 1.65f, -1.18f, 2.82f)
                        reflectiveQuadTo(15f, 21f)
                        reflectiveQuadTo(12.18f, 19.83f)
                        reflectiveQuadTo(11f, 17f)
                        verticalLineTo(7f)
                        quadTo(11f, 6.18f, 10.41f, 5.59f)
                        reflectiveQuadTo(9f, 5f)
                        quadTo(8.18f, 5f, 7.59f, 5.59f)
                        quadTo(7f, 6.18f, 7f, 7f)
                        verticalLineToRelative(7f)
                        horizontalLineTo(8f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(9f, 15f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(8f)
                        verticalLineToRelative(1f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(7f, 21f)
                        horizontalLineTo(5f)
                        close()
                    }
                }
                .build()
        return __cable!!
    }

private var __cable: ImageVector? = null
