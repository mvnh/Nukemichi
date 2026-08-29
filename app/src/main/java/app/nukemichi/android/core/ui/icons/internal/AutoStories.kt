package app.nukemichi.android.core.ui.icons.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val _auto_stories: ImageVector
    get() {
        if (__auto_stories != null) {
            return __auto_stories!!
        }
        __auto_stories =
            ImageVector.Builder(
                name = "auto_stories",
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
                        moveTo(12f, 20f)
                        quadTo(10.8f, 19.05f, 9.4f, 18.52f)
                        reflectiveQuadTo(6.5f, 18f)
                        quadTo(5.45f, 18f, 4.44f, 18.27f)
                        reflectiveQuadTo(2.5f, 19.05f)
                        quadTo(1.98f, 19.33f, 1.49f, 19.02f)
                        quadTo(1f, 18.73f, 1f, 18.15f)
                        verticalLineTo(6.1f)
                        quadTo(1f, 5.82f, 1.14f, 5.57f)
                        quadTo(1.28f, 5.32f, 1.55f, 5.2f)
                        quadTo(2.7f, 4.6f, 3.95f, 4.3f)
                        reflectiveQuadTo(6.5f, 4f)
                        quadTo(7.95f, 4f, 9.34f, 4.38f)
                        reflectiveQuadTo(12f, 5.5f)
                        verticalLineTo(17.6f)
                        quadToRelative(1.28f, -0.8f, 2.68f, -1.2f)
                        reflectiveQuadTo(17.5f, 16f)
                        quadToRelative(0.9f, 0f, 1.76f, 0.15f)
                        reflectiveQuadTo(21f, 16.6f)
                        verticalLineTo(4.6f)
                        quadToRelative(0.38f, 0.13f, 0.74f, 0.26f)
                        reflectiveQuadTo(22.45f, 5.2f)
                        quadToRelative(0.27f, 0.13f, 0.41f, 0.38f)
                        reflectiveQuadTo(23f, 6.1f)
                        verticalLineTo(18.15f)
                        quadToRelative(0f, 0.58f, -0.49f, 0.88f)
                        quadToRelative(-0.49f, 0.3f, -1.01f, 0.03f)
                        quadToRelative(-0.92f, -0.5f, -1.94f, -0.78f)
                        reflectiveQuadTo(17.5f, 18f)
                        quadTo(16f, 18f, 14.6f, 18.52f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                        moveToRelative(2f, -5f)
                        verticalLineTo(5.5f)
                        lineToRelative(5f, -5f)
                        verticalLineToRelative(10f)
                        lineTo(14f, 15f)
                        close()
                        moveToRelative(-4f, 1.63f)
                        verticalLineTo(6.72f)
                        quadTo(9.18f, 6.38f, 8.29f, 6.19f)
                        reflectiveQuadTo(6.5f, 6f)
                        quadTo(5.58f, 6f, 4.7f, 6.18f)
                        reflectiveQuadTo(3f, 6.7f)
                        verticalLineToRelative(9.93f)
                        quadTo(3.88f, 16.3f, 4.74f, 16.15f)
                        reflectiveQuadTo(6.5f, 16f)
                        reflectiveQuadToRelative(1.76f, 0.15f)
                        reflectiveQuadTo(10f, 16.63f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineTo(6.72f)
                        verticalLineToRelative(9.9f)
                        close()
                    }
                }
                .build()
        return __auto_stories!!
    }

private var __auto_stories: ImageVector? = null
