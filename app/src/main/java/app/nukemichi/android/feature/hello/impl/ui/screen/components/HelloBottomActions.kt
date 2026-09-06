package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R

@Composable
internal fun HelloBottomActions(
    onSettingsClick: () -> Unit,
    onViewAdvancedModeClick: () -> Unit,
    onViewSourceCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onSettingsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.settings_title))
        }
        TextButton(
            onClick = onViewAdvancedModeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.hello_view_advanced_mode))
        }
        TextButton(
            onClick = onViewSourceCodeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.hello_view_source_code))
        }
    }
}
