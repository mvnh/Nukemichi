package app.nukemichi.android.feature.wizard.impl.ui.screen.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.components.StatusBadge
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.core.ui.util.dashedBorder
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.WizardPage
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.SetupStrategy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun StrategyChoicePage(
    selectedStrategy: SetupStrategy?,
    onStrategySelected: (SetupStrategy) -> Unit,
    isAdvancedMode: Boolean,
    modifier: Modifier = Modifier
) {
    val dimens = MaterialTheme.dimens

    WizardPage(modifier = modifier) {
        Text(
            text = stringResource(R.string.wizard_strategy_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(dimens.l))

        StrategyCard(
            title = UiText.Resource(R.string.wizard_strategy_fast_start_title),
            description = UiText.Resource(R.string.wizard_strategy_fast_start_desc),
            icon = NukemichiIcons.Common.RocketLaunch,
            timeEstimate = UiText.Resource(R.string.wizard_tag_5_min),
            difficulty = UiText.Resource(R.string.wizard_tag_easy),
            badgeText = UiText.Resource(R.string.wizard_tag_recommended),
            techStack = persistentListOf(
                UiText.Raw("xray-core"),
                UiText.Raw("vless"),
                UiText.Raw("xhttp"),
                UiText.Raw("reality")
            ),
            isSelected = selectedStrategy == SetupStrategy.FAST_START,
            onClick = { onStrategySelected(SetupStrategy.FAST_START) },
            enabled = true,
            showTechStack = isAdvancedMode,
        )

        Spacer(modifier = Modifier.height(dimens.l))

        StrategyCard(
            title = UiText.Resource(R.string.wizard_strategy_resilience_title),
            description = UiText.Resource(R.string.wizard_strategy_resilience_desc),
            icon = NukemichiIcons.Common.Shield,
            timeEstimate = UiText.Resource(R.string.wizard_tag_15_min),
            difficulty = UiText.Resource(R.string.wizard_tag_hard),
            badgeText = UiText.Resource(R.string.wizard_tag_soon),
            techStack = persistentListOf(
                UiText.Raw("naiveproxy"),
                UiText.Resource(R.string.wizard_tag_domain)
            ),
            isSelected = selectedStrategy == SetupStrategy.NAIVEPROXY,
            onClick = { onStrategySelected(SetupStrategy.NAIVEPROXY) },
            enabled = false,
            showTechStack = isAdvancedMode,
        )
    }
}

@Composable
internal fun StrategyCard(
    title: UiText,
    description: UiText,
    icon: ImageVector,
    timeEstimate: UiText,
    difficulty: UiText,
    techStack: ImmutableList<UiText>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badgeText: UiText? = null,
    showTechStack: Boolean = false,
) {
    val dimens = MaterialTheme.dimens
    val shape = CardDefaults.shape

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val solidBorder = if (enabled && isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else null

    val dashedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (!enabled) {
                Modifier.dashedBorder(color = dashedColor, shape = shape)
            } else Modifier
        )
        .alpha(if (enabled) 1f else 0.5f)

    Card(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = cardModifier,
        border = solidBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, disabledElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.m)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(dimens.xl)
                    )

                    Text(
                        text = title.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (badgeText != null) {
                    StatusBadge(
                        text = badgeText,
                        // "Recommended" is a positive nudge and "Soon" is a neutral status.
                        // Same component; the tint is what tells them apart.
                        containerColor = if (enabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        contentColor = if (enabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.s))

            Text(
                text = description.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimens.m))

            StrategyMetadata(
                timeEstimate = timeEstimate,
                difficulty = difficulty
            )

            if (showTechStack) {
                Spacer(modifier = Modifier.height(dimens.m))
                TechStackRow(techStack = techStack)
            }
        }
    }
}

@Composable
internal fun StrategyMetadata(
    timeEstimate: UiText,
    difficulty: UiText,
    modifier: Modifier = Modifier
) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.s)
    ) {
        Icon(
            imageVector = NukemichiIcons.Common.Schedule,
            contentDescription = null,
            modifier = Modifier.size(dimens.l),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = timeEstimate.asString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "•",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = difficulty.asString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TechStackRow(
    techStack: ImmutableList<UiText>,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimens.m),
        verticalArrangement = Arrangement.spacedBy(dimens.xs)
    ) {
        techStack.forEach { tech ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.xs)
            ) {
                Icon(
                    imageVector = NukemichiIcons.Common.Cable,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.m),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = tech.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
