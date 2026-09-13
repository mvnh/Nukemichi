package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import app.nukemichi.android.R
import app.nukemichi.android.platform.ui.components.MonospaceLogList
import app.nukemichi.android.platform.ui.icons.NukemichiIcons
import app.nukemichi.android.platform.ui.theme.size.dimens
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

    // Owned here, not by the placeholder, which leaves composition whenever the terminal is shown:
    // switching back resumes the rotation instead of restarting it. It only advances while the
    // placeholder is on screen.
    val idleMessages = stringArrayResource(R.array.wizard_deployment_idle_messages)
    var idleMessageIndex by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(isExpanded, idleMessages.size) {
        if (isExpanded) return@LaunchedEffect
        while (true) {
            delay(IDLE_MESSAGE_ROTATION_MS)
            idleMessageIndex = (idleMessageIndex + 1) % idleMessages.size
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Clipped to the button's own pill, so a changing label never squares its ends off.
            TextButton(
                onClick = onToggleExpanded,
                modifier = Modifier
                    .clip(CircleShape)
                    .animateContentSize(),
            ) {
                Icon(
                    imageVector = if (isExpanded) NukemichiIcons.Outlined.ArrowDropUp else NukemichiIcons.Outlined.ArrowDropDown,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(
                        if (isExpanded) R.string.wizard_deployment_terminal_hide else R.string.wizard_deployment_terminal_show
                    )
                )
            }

            AnimatedVisibility(visible = isExpanded, modifier = Modifier.clip(CircleShape)) {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(logLines.joinToString("\n")))
                        justCopied = true
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .animateContentSize(),
                ) {
                    if (justCopied) {
                        Icon(imageVector = NukemichiIcons.Outlined.Check, contentDescription = null)
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
            Crossfade(targetState = isExpanded, label = "terminal_content_transition") { showsTerminal ->
                if (showsTerminal) {
                    MonospaceLogList(
                        lines = logLines,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(dimens.m),
                    )
                } else {
                    IdlePlaceholder(
                        message = idleMessages[idleMessageIndex % idleMessages.size],
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun IdlePlaceholder(message: String, modifier: Modifier = Modifier) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(dimens.xxl + dimens.xl)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = IDLE_BLOB_ALPHA),
                            shape = CircleShape,
                        ),
                )
                Image(
                    painter = painterResource(R.drawable.michi_cat),
                    contentDescription = null,
                    // The drawable is flat black line art, which disappears on a dark surface.
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .size(dimens.successBadge)
                        .graphicsLayer { translationY = -bounce * IDLE_BOUNCE_PX },
                )
            }
            // SizeTransform eases the text block, and the cat above it, between messages of
            // different length instead of snapping to the new height.
            AnimatedContent(
                targetState = message,
                transitionSpec = { (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false)) },
                contentAlignment = Alignment.TopCenter,
                label = "idle_message_transition",
                modifier = Modifier.padding(top = dimens.l),
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private const val COPY_FEEDBACK_MS = 1_500L
private const val IDLE_MESSAGE_ROTATION_MS = 3_200L
private const val IDLE_BOUNCE_PX = 10f
private const val IDLE_BLOB_ALPHA = 0.35f
