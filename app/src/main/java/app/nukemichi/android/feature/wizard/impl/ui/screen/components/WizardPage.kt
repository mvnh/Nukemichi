package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.util.WIZARD_ANIMATION_DURATION

private const val PAGE_BOTTOM_EDGE_SHADOW_ALPHA = 0.12f

@Composable
fun WizardPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val dimens = MaterialTheme.dimens
    val shadowColor = MaterialTheme.colorScheme.onSurface.copy(PAGE_BOTTOM_EDGE_SHADOW_ALPHA)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(dimens.l),
            content = content
        )

        AnimatedVisibility(
            visible = scrollState.canScrollForward,
            enter = fadeIn(animationSpec = tween(WIZARD_ANIMATION_DURATION)),
            exit = fadeOut(animationSpec = tween(WIZARD_ANIMATION_DURATION)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.slimXxl)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, shadowColor)
                        )
                    )
            )
        }
    }
}
