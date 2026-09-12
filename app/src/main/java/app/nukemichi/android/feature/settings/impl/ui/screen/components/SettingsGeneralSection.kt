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
import app.nukemichi.android.platform.ui.theme.size.dimens

// Visible in every AppMode: viewing logs isn't an expert-only action.
@Composable
internal fun SettingsGeneralSection(
    onViewLogsClick: () -> Unit,
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
