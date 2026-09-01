package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens

@Composable
internal fun DeploymentSuccessCelebration(
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    val badgeScale = remember { Animatable(1.6f) }
    val badgeAlpha = remember { Animatable(0f) }
    val badgeRotation = remember { Animatable(-6f) }
    val contentAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // A quick, decisive "stamp" — slams down from slightly oversized/tilted to rest, no bounce.
        badgeAlpha.animateTo(1f, tween(120))
        badgeScale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        badgeRotation.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
        contentAlpha.animateTo(1f, tween(300, delayMillis = 100))
        buttonAlpha.animateTo(1f, tween(300, delayMillis = 150))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(dimens.successBadge)
                .graphicsLayer {
                    alpha = badgeAlpha.value
                    scaleX = badgeScale.value
                    scaleY = badgeScale.value
                    rotationZ = badgeRotation.value
                }
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NukemichiIcons.Common.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(dimens.xl),
            )
        }

        Spacer(modifier = Modifier.size(dimens.s))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = contentAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.wizard_deployment_success_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.wizard_deployment_success_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = dimens.s),
            )
        }

        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimens.xl)
                .graphicsLayer { alpha = buttonAlpha.value },
        ) {
            Text(text = stringResource(R.string.wizard_deployment_success_cta))
        }
    }
}
