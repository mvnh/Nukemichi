package app.nukemichi.android.feature.settings.impl.ui.screen

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.nukemichi.android.R
import app.nukemichi.android.feature.settings.impl.ui.mvi.SettingsContract
import app.nukemichi.android.feature.settings.impl.ui.mvi.SettingsViewModel
import app.nukemichi.android.feature.settings.impl.ui.screen.components.SettingsDangerZoneSection
import app.nukemichi.android.feature.settings.impl.ui.screen.components.SettingsGeneralSection
import app.nukemichi.android.platform.mode.AppMode
import app.nukemichi.android.platform.ui.theme.size.dimens
import app.nukemichi.android.platform.ui.util.CollectAsEffect

@Composable
internal fun SettingsScreen(
    onNavigateToLogs: () -> Unit,
    onNavigateToAdvancedModeIntro: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            SettingsContract.Effect.NavigateToAdvancedModeIntro -> onNavigateToAdvancedModeIntro()
            SettingsContract.Effect.NavigateToLogs -> onNavigateToLogs()
        }
    }

    SettingsContent(
        state = state,
        onViewLogsClick = { viewModel.processIntent(SettingsContract.Intent.ViewLogsRequested) },
        onAdvancedModeToggled = { enabled ->
            viewModel.processIntent(SettingsContract.Intent.AdvancedModeToggled(enabled))
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsContract.State,
    onViewLogsClick: () -> Unit,
    onAdvancedModeToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(dimens.l),
        verticalArrangement = Arrangement.spacedBy(dimens.l),
    ) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SettingsGeneralSection(onViewLogsClick = onViewLogsClick)

        SettingsDangerZoneSection(
            advancedModeEnabled = state.mode == AppMode.ADVANCED,
            onAdvancedModeToggled = onAdvancedModeToggled,
        )
    }
}
