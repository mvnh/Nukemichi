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
internal val _check_outlined: ImageVector
    get() {
        if (__check_outlined != null) {
            return __check_outlined!!
        }
        __check_outlined =
            ImageVector.Builder(
                name = "check",
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
                        moveTo(9.55f, 18f)
                        lineTo(3.85f, 12.3f)
                        lineTo(5.28f, 10.88f)
                        lineToRelative(4.28f, 4.28f)
                        lineTo(18.73f, 5.97f)
                        lineTo(20.15f, 7.4f)
                        lineTo(9.55f, 18f)
                        close()
                    }
                }
                .build()
        return __check_outlined!!
    }

private var __check_outlined: ImageVector? = null
