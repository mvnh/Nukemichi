package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.components.MonospaceLogList
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay

@Composable
internal fun LiveTerminalView(
    logLines: ImmutableList<String>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    val clipboard = LocalClipboardManager.current
    var justCopied by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(COPY_FEEDBACK_MS)
            justCopied = false
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onToggleExpanded) {
                Icon(
                    imageVector = if (isExpanded) NukemichiIcons.Navigation.ArrowDropUp else NukemichiIcons.Navigation.ArrowDropDown,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(
                        if (isExpanded) R.string.wizard_deployment_terminal_hide else R.string.wizard_deployment_terminal_show
                    )
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(logLines.joinToString("\n")))
                        justCopied = true
                    }
                ) {
                    if (justCopied) {
                        Icon(imageVector = NukemichiIcons.Common.Check, contentDescription = null)
                    }
                    Text(
                        text = stringResource(
                            if (justCopied) R.string.wizard_deployment_terminal_copied
                            else R.string.wizard_deployment_terminal_copy
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            if (isExpanded) {
                MonospaceLogList(
                    lines = logLines,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(dimens.m),
                )
            } else {
                IdlePlaceholder(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun IdlePlaceholder(modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens
    val transition = rememberInfiniteTransition(label = "idle_cat_bounce")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "idle_cat_bounce_value",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.michi_cat),
                contentDescription = null,
                modifier = Modifier
                    .size(dimens.control)
                    .graphicsLayer { translationY = -bounce * 8f },
            )
            Text(
                text = stringResource(R.string.wizard_deployment_idle_message),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimens.s),
            )
        }
    }
}

private const val COPY_FEEDBACK_MS = 1_500L
