package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText

@Composable
internal fun HelloContent(
    onSetUpServerClick: () -> Unit,
    onConnectOrImportClick: () -> Unit,
    onLearnFirstClick: () -> Unit,
    onViewSourceCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val dimens = MaterialTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(scrollState)
            .padding(horizontal = dimens.l, vertical = dimens.l),
        // Centers the block when it's shorter than the viewport (the common case); once content
        // overflows, verticalScroll naturally takes over and this has no effect.
        verticalArrangement = Arrangement.spacedBy(dimens.m, Alignment.CenterVertically)
    ) {
        HelloHeader(subtitle = UiText.Resource(R.string.hello_header_subtitle))

        HelloActionCard(
            title = UiText.Resource(R.string.hello_setup_title),
            description = UiText.Resource(R.string.hello_setup_description),
            icon = NukemichiIcons.Common.Dns,
            onClick = onSetUpServerClick,
        )

        HelloActionCard(
            title = UiText.Resource(R.string.hello_connect_title),
            description = UiText.Resource(R.string.hello_connect_description),
            icon = NukemichiIcons.Common.Cable,
            onClick = onConnectOrImportClick,
            enabled = false,
            badgeText = UiText.Resource(R.string.hello_badge_soon),
        )

        HelloActionCard(
            title = UiText.Resource(R.string.hello_learn_title),
            description = UiText.Resource(R.string.hello_learn_description),
            icon = NukemichiIcons.Common.AutoStories,
            onClick = onLearnFirstClick,
            enabled = false,
            badgeText = UiText.Resource(R.string.hello_badge_soon),
        )

        HelloBottomActions(
            onViewSourceCodeClick = onViewSourceCodeClick,
        )
    }
}
