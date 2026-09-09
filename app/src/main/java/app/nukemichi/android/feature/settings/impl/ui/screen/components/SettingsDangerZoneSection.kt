package app.nukemichi.android.feature.settings.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.nukemichi.android.R
import app.nukemichi.android.platform.ui.theme.size.dimens

// Deliberately at the bottom, deliberately styled apart from every other row on this screen:
// this switch is the only thing on the page that can turn Advanced mode ON. Turning it off
// stays a plain, ungated flip — only unlocking the risk-gated features needs the friction.
@Composable
internal fun SettingsDangerZoneSection(
    advancedModeEnabled: Boolean,
    onAdvancedModeToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(modifier = Modifier.padding(dimens.l)) {
            Text(
                text = stringResource(id = R.string.settings_danger_zone_title),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimens.m),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.settings_advanced_mode_toggle_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.settings_advanced_mode_toggle_description),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = advancedModeEnabled,
                    onCheckedChange = onAdvancedModeToggled,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.error),
                )
            }
        }
    }
}
