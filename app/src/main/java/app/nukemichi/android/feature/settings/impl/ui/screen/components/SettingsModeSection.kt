package app.nukemichi.android.feature.settings.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import app.nukemichi.android.R
import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.ui.theme.size.dimens

@Composable
internal fun SettingsModeSection(
    mode: AppMode,
    onModeSelected: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimens.m)) {
        Text(
            text = stringResource(id = R.string.settings_mode_section_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(dimens.s),
        ) {
            ModeOption(
                title = stringResource(id = R.string.settings_mode_normal_title),
                description = stringResource(id = R.string.settings_mode_normal_description),
                selected = mode == AppMode.NORMAL,
                onClick = { onModeSelected(AppMode.NORMAL) },
            )
            ModeOption(
                title = stringResource(id = R.string.settings_mode_advanced_title),
                description = stringResource(id = R.string.settings_mode_advanced_description),
                selected = mode == AppMode.ADVANCED,
                onClick = { onModeSelected(AppMode.ADVANCED) },
            )
        }
    }
}

// Plain String, not UiText: private helper always fed already-resolved stringResource(...)
// inline at its two call sites, no reuse outside this file, no MVI-state boundary to cross.
@Composable
private fun ModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.m),
        ) {
            RadioButton(selected = selected, onClick = null)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
