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
internal val _rocket_launch_filled: ImageVector
    get() {
        if (__rocket_launch_filled != null) {
            return __rocket_launch_filled!!
        }
        __rocket_launch_filled =
            ImageVector.Builder(
                name = "rocket_launch",
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
                        moveTo(2.45f, 10.58f)
                        lineToRelative(4.2f, -4.2f)
                        quadTo(7f, 6.02f, 7.48f, 5.88f)
                        quadTo(7.95f, 5.72f, 8.45f, 5.82f)
                        lineTo(9.75f, 6.1f)
                        quadTo(8.4f, 7.7f, 7.63f, 9f)
                        reflectiveQuadToRelative(-1.5f, 3.15f)
                        lineTo(2.45f, 10.58f)
                        close()
                        moveToRelative(5.13f, 2.28f)
                        quadToRelative(0.58f, -1.8f, 1.56f, -3.4f)
                        reflectiveQuadToRelative(2.39f, -3f)
                        quadToRelative(2.2f, -2.2f, 5.03f, -3.29f)
                        reflectiveQuadTo(21.83f, 2.5f)
                        quadToRelative(0.42f, 2.45f, -0.65f, 5.27f)
                        reflectiveQuadTo(17.9f, 12.8f)
                        quadToRelative(-1.38f, 1.38f, -3f, 2.39f)
                        quadToRelative(-1.63f, 1.01f, -3.42f, 1.59f)
                        lineTo(7.58f, 12.85f)
                        close()
                        moveToRelative(8.31f, -2.43f)
                        quadToRelative(0.84f, 0f, 1.41f, -0.57f)
                        quadTo(17.88f, 9.27f, 17.88f, 8.44f)
                        reflectiveQuadTo(17.3f, 7.02f)
                        reflectiveQuadTo(15.89f, 6.45f)
                        reflectiveQuadTo(14.48f, 7.02f)
                        reflectiveQuadTo(13.9f, 8.44f)
                        quadToRelative(0f, 0.84f, 0.58f, 1.41f)
                        reflectiveQuadToRelative(1.41f, 0.57f)
                        close()
                        moveTo(13.78f, 21.88f)
                        lineTo(12.18f, 18.2f)
                        quadToRelative(1.85f, -0.72f, 3.16f, -1.5f)
                        quadToRelative(1.31f, -0.78f, 2.91f, -2.13f)
                        lineToRelative(0.25f, 1.3f)
                        quadToRelative(0.1f, 0.5f, -0.05f, 0.99f)
                        reflectiveQuadToRelative(-0.5f, 0.84f)
                        lineToRelative(-4.17f, 4.18f)
                        close()
                        moveTo(4.05f, 16.05f)
                        quadTo(4.93f, 15.18f, 6.18f, 15.16f)
                        reflectiveQuadTo(8.3f, 16.02f)
                        reflectiveQuadToRelative(0.88f, 2.13f)
                        reflectiveQuadTo(8.3f, 20.27f)
                        quadTo(7.68f, 20.9f, 6.21f, 21.35f)
                        reflectiveQuadToRelative(-4.04f, 0.8f)
                        quadToRelative(0.35f, -2.57f, 0.8f, -4.02f)
                        reflectiveQuadTo(4.05f, 16.05f)
                        close()
                    }
                }
                .build()
        return __rocket_launch_filled!!
    }

private var __rocket_launch_filled: ImageVector? = null
