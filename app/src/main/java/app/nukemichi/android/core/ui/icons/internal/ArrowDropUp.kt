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
internal val _arrow_drop_up: ImageVector
    get() {
        if (__arrow_drop_up != null) {
            return __arrow_drop_up!!
        }
        __arrow_drop_up =
            ImageVector.Builder(
                name = "arrow_drop_up",
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
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(7f, 14f)
                        lineTo(12f, 9f)
                        lineToRelative(5f, 5f)
                        horizontalLineTo(7f)
                        close()
                    }
                }
                .build()
        return __arrow_drop_up!!
    }

private var __arrow_drop_up: ImageVector? = null