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
internal val _rocket_launch: ImageVector
    get() {
        if (__rocket_launch != null) {
            return __rocket_launch!!
        }
        __rocket_launch =
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
                        moveTo(5.65f, 10.02f)
                        lineTo(7.6f, 10.85f)
                        quadTo(7.95f, 10.15f, 8.33f, 9.5f)
                        reflectiveQuadTo(9.15f, 8.2f)
                        lineTo(7.75f, 7.93f)
                        lineToRelative(-2.1f, 2.1f)
                        close()
                        moveTo(9.2f, 12.1f)
                        lineToRelative(2.85f, 2.82f)
                        quadTo(13.1f, 14.53f, 14.3f, 13.7f)
                        quadToRelative(1.2f, -0.82f, 2.25f, -1.88f)
                        quadTo(18.3f, 10.07f, 19.29f, 7.94f)
                        reflectiveQuadTo(20.15f, 4f)
                        quadTo(18.35f, 3.88f, 16.2f, 4.86f)
                        reflectiveQuadTo(12.3f, 7.6f)
                        quadTo(11.25f, 8.65f, 10.43f, 9.85f)
                        reflectiveQuadTo(9.2f, 12.1f)
                        close()
                        moveTo(13.08f, 9.06f)
                        quadToRelative(0f, -0.84f, 0.57f, -1.41f)
                        reflectiveQuadTo(15.08f, 7.07f)
                        reflectiveQuadTo(16.5f, 7.65f)
                        quadToRelative(0.57f, 0.58f, 0.57f, 1.41f)
                        reflectiveQuadTo(16.5f, 10.48f)
                        reflectiveQuadToRelative(-1.42f, 0.57f)
                        reflectiveQuadTo(13.65f, 10.48f)
                        reflectiveQuadTo(13.08f, 9.06f)
                        close()
                        moveToRelative(1.05f, 9.44f)
                        lineToRelative(2.1f, -2.1f)
                        lineTo(15.95f, 15f)
                        quadToRelative(-0.65f, 0.45f, -1.3f, 0.81f)
                        reflectiveQuadTo(13.3f, 16.52f)
                        lineToRelative(0.82f, 1.98f)
                        close()
                        moveTo(21.95f, 2.17f)
                        quadTo(22.43f, 5.2f, 21.36f, 8.06f)
                        reflectiveQuadTo(17.7f, 13.52f)
                        lineTo(18.2f, 16f)
                        quadToRelative(0.1f, 0.5f, -0.05f, 0.98f)
                        reflectiveQuadToRelative(-0.5f, 0.82f)
                        lineTo(13.45f, 22f)
                        lineToRelative(-2.1f, -4.93f)
                        lineTo(7.08f, 12.8f)
                        lineTo(2.15f, 10.7f)
                        lineTo(6.33f, 6.5f)
                        quadTo(6.68f, 6.15f, 7.16f, 6f)
                        reflectiveQuadTo(8.15f, 5.95f)
                        lineToRelative(2.47f, 0.5f)
                        quadToRelative(2.6f, -2.6f, 5.45f, -3.68f)
                        quadTo(18.93f, 1.7f, 21.95f, 2.17f)
                        close()
                        moveTo(3.93f, 15.98f)
                        quadTo(4.8f, 15.1f, 6.06f, 15.09f)
                        quadTo(7.33f, 15.08f, 8.2f, 15.95f)
                        reflectiveQuadToRelative(0.86f, 2.14f)
                        reflectiveQuadTo(8.18f, 20.23f)
                        quadTo(7.55f, 20.85f, 6.09f, 21.3f)
                        reflectiveQuadTo(2.05f, 22.1f)
                        quadTo(2.4f, 19.52f, 2.85f, 18.06f)
                        reflectiveQuadTo(3.93f, 15.98f)
                        close()
                        moveToRelative(1.43f, 1.4f)
                        quadTo(5.1f, 17.63f, 4.85f, 18.29f)
                        reflectiveQuadTo(4.5f, 19.63f)
                        quadToRelative(0.68f, -0.1f, 1.34f, -0.34f)
                        reflectiveQuadTo(6.75f, 18.8f)
                        quadToRelative(0.3f, -0.3f, 0.33f, -0.73f)
                        reflectiveQuadTo(6.8f, 17.35f)
                        reflectiveQuadTo(6.08f, 17.06f)
                        reflectiveQuadTo(5.35f, 17.38f)
                        close()
                    }
                }
                .build()
        return __rocket_launch!!
    }

private var __rocket_launch: ImageVector? = null
