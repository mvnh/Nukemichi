package app.nukemichi.android.platform.ui.icons.sources

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val _vpn_key_outlined: ImageVector
    get() {
        if (__vpn_key_outlined != null) {
            return __vpn_key_outlined!!
        }
        __vpn_key_outlined =
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
                        quadToRelative(1.65f, 0f, 3.03f, 0.82f)
                        reflectiveQuadTo(12.2f, 9f)
                        horizontalLineTo(23f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(21f)
                        verticalLineToRelative(3f)
                        horizontalLineTo(15f)
                        verticalLineTo(15f)
                        horizontalLineTo(12.2f)
                        quadToRelative(-0.8f, 1.35f, -2.18f, 2.18f)
                        reflectiveQuadTo(7f, 18f)
                        close()
                        moveTo(7f, 16f)
                        quadToRelative(1.65f, 0f, 2.65f, -1.01f)
                        quadToRelative(1f, -1.01f, 1.2f, -1.99f)
                        horizontalLineTo(17f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(13f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(11f)
                        horizontalLineTo(10.85f)
                        quadTo(10.65f, 10.02f, 9.65f, 9.01f)
                        reflectiveQuadTo(7f, 8f)
                        reflectiveQuadTo(4.18f, 9.17f)
                        reflectiveQuadTo(3f, 12f)
                        reflectiveQuadToRelative(1.17f, 2.82f)
                        reflectiveQuadTo(7f, 16f)
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
                        moveTo(7f, 12f)
                        close()
                    }
                }
                .build()
        return __vpn_key_outlined!!
    }

private var __vpn_key_outlined: ImageVector? = null
