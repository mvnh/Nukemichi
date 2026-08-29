package app.nukemichi.android.feature.hello.impl.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.feature.hello.impl.ui.mvi.AdvancedModeIntroViewModel

@Composable
internal fun AdvancedModeIntroScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdvancedModeIntroViewModel = hiltViewModel(),
) {
    var risksAcknowledged by remember { mutableStateOf(false) }

    AdvancedModeIntroContent(
        risksAcknowledged = risksAcknowledged,
        onRisksAcknowledgedChanged = { risksAcknowledged = it },
        onBackClick = onBackClick,
        onEnableClick = {
            viewModel.confirmAdvancedMode()
            onBackClick()
        },
        modifier = modifier,
    )
}

@Composable
private fun AdvancedModeIntroContent(
    risksAcknowledged: Boolean,
    onRisksAcknowledgedChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimens.m, end = dimens.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = NukemichiIcons.Navigation.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(id = R.string.advanced_mode_intro_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.l),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(dimens.l)) {
                    Text(
                        text = stringResource(id = R.string.advanced_mode_intro_capabilities_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(dimens.m))
                    BulletPoint(stringResource(id = R.string.advanced_mode_intro_capability_transport))
                    BulletPoint(stringResource(id = R.string.advanced_mode_intro_capability_config))
                    BulletPoint(stringResource(id = R.string.advanced_mode_intro_capability_stats))
                    BulletPoint(stringResource(id = R.string.advanced_mode_intro_capability_tty))
                }
            }

            Spacer(modifier = Modifier.height(dimens.l))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Column(modifier = Modifier.padding(dimens.l)) {
                    Text(
                        text = stringResource(id = R.string.advanced_mode_intro_warning_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(modifier = Modifier.height(dimens.s))
                    Text(
                        text = stringResource(id = R.string.advanced_mode_intro_warning_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.l))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRisksAcknowledgedChanged(!risksAcknowledged) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.s),
            ) {
                Checkbox(checked = risksAcknowledged, onCheckedChange = onRisksAcknowledgedChanged)
                Text(
                    text = stringResource(id = R.string.advanced_mode_intro_acknowledge),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(dimens.l))
        }

        Button(
            onClick = onEnableClick,
            enabled = risksAcknowledged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.l)
                .padding(bottom = dimens.xl),
        ) {
            Text(text = stringResource(id = R.string.advanced_mode_intro_enable))
        }
    }
}

@Composable
private fun BulletPoint(text: String, modifier: Modifier = Modifier) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimens.xs),
        horizontalArrangement = Arrangement.spacedBy(dimens.s),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
