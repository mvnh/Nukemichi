package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.util.WIZARD_ANIMATION_DURATION
import app.nukemichi.android.feature.wizard.impl.ui.model.WizardScopeImpl
import app.nukemichi.android.feature.wizard.impl.ui.model.WizardState
import app.nukemichi.android.feature.wizard.impl.ui.model.WizardStep
import kotlinx.collections.immutable.ImmutableList

private val slideAnimationSpec = tween<IntOffset>(
    durationMillis = WIZARD_ANIMATION_DURATION,
    easing = FastOutSlowInEasing
)
private val fadeAnimationSpec = tween<Float>(
    durationMillis = WIZARD_ANIMATION_DURATION,
    easing = FastOutSlowInEasing
)

@Composable
fun WizardContainer(
    modifier: Modifier = Modifier,
    state: WizardState,
    title: UiText,
    steps: ImmutableList<WizardStep>,
    onNavIconClick: () -> Unit,
    onNextClick: () -> Unit,
    isLoading: Boolean = false,
    isNextEnabled: Boolean = true,
    disabledReasonText: UiText = UiText.Empty,
    nextButtonText: UiText = UiText.Resource(R.string.next),
    finishButtonText: UiText = UiText.Resource(R.string.finish),
    backButtonText: UiText = UiText.Resource(R.string.back),
    isTopBarVisible: Boolean = true,
    isBottomBarVisible: Boolean = true,
    topBar: @Composable () -> Unit = {
        WizardTopBar(title = title, state = state, onNavIconClick = onNavIconClick)
    }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val dimens = MaterialTheme.dimens
    val scope = remember(state) { WizardScopeImpl(state) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val density = LocalDensity.current

    val cardShape = MaterialTheme.shapes.large.copy(
        topStart = CornerSize(dimens.s)
    )

    val slideOffsetPx = remember(density) { with(density) { dimens.xl.roundToPx() } }

    BackHandler(enabled = !state.isFirstPage && !isLoading && isBottomBarVisible) {
        keyboardController?.hide()
        state.previous()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .animateContentSize()
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
    ) {
        AnimatedVisibility(
            visible = isTopBarVisible,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            topBar()
        }

        if (steps.any { it.title !is UiText.Empty }) {
            val fadeWidthPx = with(density) { dimens.l.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimens.s)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()

                        val w = size.width
                        val h = size.height

                        if (w > 0f) {
                            val leftStop = (fadeWidthPx / w).coerceIn(0f, 0.5f)
                            val rightStop = ((w - fadeWidthPx) / w).coerceIn(0.5f, 1f)

                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    leftStop to Color.Black,
                                    rightStop to Color.Black,
                                    1.0f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
            ) {
                AnimatedContent(
                    targetState = state.currentPage,
                    label = "step_title_transition",
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (
                                slideInHorizontally(animationSpec = slideAnimationSpec) { slideOffsetPx * direction } +
                                        fadeIn(animationSpec = fadeAnimationSpec)
                                ).togetherWith(
                                slideOutHorizontally(animationSpec = slideAnimationSpec) { -slideOffsetPx * direction } +
                                        fadeOut(animationSpec = fadeAnimationSpec)
                            ).using(SizeTransform(clip = false))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.l)
                ) { page ->
                    val stepTitle = steps.getOrNull(page)?.title ?: UiText.Empty
                    if (stepTitle !is UiText.Empty) {
                        Text(
                            text = stepTitle.asString(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = dimens.l)
                .padding(bottom = dimens.m),
            shape = cardShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AnimatedContent(
                targetState = state.currentPage,
                label = "wizard_screen_transition",
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (
                            slideInHorizontally(animationSpec = slideAnimationSpec) { width -> width * direction } +
                                    fadeIn(animationSpec = fadeAnimationSpec)
                            ).togetherWith(
                            slideOutHorizontally(animationSpec = slideAnimationSpec) { width -> -width * direction } +
                                    fadeOut(animationSpec = fadeAnimationSpec)
                        ).using(SizeTransform(clip = false))
                },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                saveableStateHolder.SaveableStateProvider(key = page) {
                    steps.getOrNull(page)?.content?.invoke(scope)
                }
            }
        }

        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            WizardBottomBar(
                state = state,
                isLoading = isLoading,
                isNextEnabled = isNextEnabled,
                disabledReasonText = disabledReasonText,
                nextButtonText = if (state.isLastPage) finishButtonText else nextButtonText,
                backButtonText = backButtonText,
                onBackClick = {
                    keyboardController?.hide()
                    state.previous()
                },
                onNextClick = {
                    keyboardController?.hide()
                    onNextClick()
                }
            )
        }
    }
}

@Composable
fun WizardTopBar(
    title: UiText,
    state: WizardState,
    isNavButtonVisible: Boolean = true,
    onNavIconClick: () -> Unit,
    onOverflowClick: (() -> Unit)? = null,
) {
    val navIcon =
        if (state.isFirstPage) NukemichiIcons.Navigation.ArrowBack else NukemichiIcons.Navigation.Close
    val dimens = MaterialTheme.dimens

    val progress by animateFloatAsState(
        targetValue = (state.currentPage + 1).toFloat() / state.pageCount,
        animationSpec = tween(400),
        label = "progress_animation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimens.s, start = dimens.m, end = dimens.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isNavButtonVisible) {
            IconButton(onClick = onNavIconClick) {
                Icon(navIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Spacer(modifier = Modifier.width(dimens.l))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = title.asString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(dimens.m))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.slimM)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        if (onOverflowClick != null) {
            IconButton(onClick = onOverflowClick) {
                Icon(
                    NukemichiIcons.Navigation.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WizardBottomBar(
    state: WizardState,
    isLoading: Boolean,
    isNextEnabled: Boolean,
    disabledReasonText: UiText,
    nextButtonText: UiText,
    backButtonText: UiText,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.l)
            .padding(top = dimens.m, bottom = dimens.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = !isNextEnabled && disabledReasonText !is UiText.Empty && !isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = disabledReasonText.asString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = dimens.m)
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            if (!state.isFirstPage) {
                TextButton(
                    onClick = onBackClick,
                    enabled = !isLoading,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(text = backButtonText.asString())
                }
            }

            Button(
                onClick = onNextClick,
                enabled = isNextEnabled && !isLoading,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.5f)
                    .animateContentSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimens.l),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimens.xs
                    )
                } else {
                    Text(text = nextButtonText.asString())
                }
            }
        }
    }
}
