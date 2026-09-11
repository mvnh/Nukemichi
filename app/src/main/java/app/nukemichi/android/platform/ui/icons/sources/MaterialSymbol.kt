package app.nukemichi.android.platform.ui.icons.sources

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

/**
 * A 24dp icon built straight from Material Symbols SVG path data. That data is drawn on a 960-unit grid
 * whose viewBox starts at y = -960, hence the group shifting it back down into the viewport.
 */
internal fun materialSymbol(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    )
        .group(translationY = 960f) {
            addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
        }
        .build()
