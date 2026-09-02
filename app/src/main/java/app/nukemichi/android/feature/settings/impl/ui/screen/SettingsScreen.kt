package app.nukemichi.android.feature.settings.impl.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.nukemichi.android.R
import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.CollectAsEffect
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.feature.settings.impl.ui.mvi.SettingsContract
import app.nukemichi.android.feature.settings.impl.ui.mvi.SettingsViewModel
import app.nukemichi.android.feature.settings.impl.ui.mvi.isAdvanced
import app.nukemichi.android.feature.settings.impl.ui.screen.components.SettingsAdvancedSection
import app.nukemichi.android.feature.settings.impl.ui.screen.components.SettingsModeSection

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is SettingsContract.Effect.ShareVlessLink -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.uri)
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }
        }
    }

    SettingsContent(
        state = state,
        onModeSelected = { mode -> viewModel.processIntent(SettingsContract.Intent.ModeChanged(mode)) },
        onRealityServerNameChanged = { value ->
            viewModel.processIntent(SettingsContract.Intent.RealityServerNameChanged(value))
        },
        onFingerprintChanged = { value ->
            viewModel.processIntent(SettingsContract.Intent.FingerprintChanged(value))
        },
        onMuxEnabledChanged = { enabled ->
            viewModel.processIntent(SettingsContract.Intent.MuxEnabledChanged(enabled))
        },
        onMuxConcurrencyChanged = { value ->
            viewModel.processIntent(SettingsContract.Intent.MuxConcurrencyChanged(value))
        },
        onExportVlessLinkClick = {
            viewModel.processIntent(SettingsContract.Intent.ExportVlessLinkRequested)
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsContract.State,
    onModeSelected: (AppMode) -> Unit,
    onRealityServerNameChanged: (String) -> Unit,
    onFingerprintChanged: (XrayFingerprint) -> Unit,
    onMuxEnabledChanged: (Boolean) -> Unit,
    onMuxConcurrencyChanged: (Int) -> Unit,
    onExportVlessLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(dimens.l),
        verticalArrangement = Arrangement.spacedBy(dimens.l),
    ) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SettingsModeSection(mode = state.mode, onModeSelected = onModeSelected)

        if (state.isAdvanced && state.hasProfile) {
            HorizontalDivider()
            SettingsAdvancedSection(
                state = state,
                onRealityServerNameChanged = onRealityServerNameChanged,
                onFingerprintChanged = onFingerprintChanged,
                onMuxEnabledChanged = onMuxEnabledChanged,
                onMuxConcurrencyChanged = onMuxConcurrencyChanged,
                onExportVlessLinkClick = onExportVlessLinkClick,
            )
        }
    }
}
