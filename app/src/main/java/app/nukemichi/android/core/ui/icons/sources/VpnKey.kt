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
internal val _vpn_key: ImageVector
    get() {
        if (__vpn_key != null) {
            return __vpn_key!!
        }
        __vpn_key =
            ImageVector.Builder(
                name = "vpn_key",
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
                        moveTo(7f, 18f)
                        quadTo(4.5f, 18f, 2.75f, 16.25f)
                        reflectiveQuadTo(1f, 12f)
                        reflectiveQuadTo(2.75f, 7.75f)
                        reflectiveQuadTo(7f, 6f)
                        quadToRelative(2.03f, 0f, 3.54f, 1.14f)
                        reflectiveQuadTo(12.65f, 10f)
                        horizontalLineTo(23f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(21f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(17f)
                        verticalLineTo(14f)
                        horizontalLineTo(12.65f)
                        quadToRelative(-0.6f, 1.72f, -2.11f, 2.86f)
                        reflectiveQuadTo(7f, 18f)
                        close()
                        moveTo(7f, 14f)
                        quadToRelative(0.83f, 0f, 1.41f, -0.59f)
                        reflectiveQuadTo(9f, 12f)
                        reflectiveQuadTo(8.41f, 10.59f)
                        quadTo(7.83f, 10f, 7f, 10f)
                        reflectiveQuadTo(5.59f, 10.59f)
                        quadTo(5f, 11.18f, 5f, 12f)
                        reflectiveQuadToRelative(0.59f, 1.41f)
                        reflectiveQuadTo(7f, 14f)
                        close()
                    }
                }
                .build()
        return __vpn_key!!
    }

private var __vpn_key: ImageVector? = null
