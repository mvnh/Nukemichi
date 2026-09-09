package app.nukemichi.android.feature.settings.impl.ui.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.theme.size.dimens

// Always visible regardless of AppMode: viewing logs or removing a saved server isn't an
// expert-only action, it just used to live in the wrong place (a text button in Dashboard's
// top bar, and nowhere at all, respectively).
@Composable
internal fun SettingsGeneralSection(
    hasProfile: Boolean,
    onViewLogsClick: () -> Unit,
    onForgetServerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimens.m)) {
        Text(
            text = stringResource(id = R.string.settings_general_section_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsActionRow(
            title = stringResource(id = R.string.settings_view_logs_title),
            description = stringResource(id = R.string.settings_view_logs_description),
            onClick = onViewLogsClick,
        )

        if (hasProfile) {
            SettingsActionRow(
                title = stringResource(id = R.string.settings_forget_server_title),
                description = stringResource(id = R.string.settings_forget_server_description),
                onClick = onForgetServerClick,
                titleColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun SettingsActionRow(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
