package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R

// Advanced mode entry point is hidden for now — it's pre-MVP, not built out yet (see project
// notes). Only the source-code link stays.
@Composable
internal fun HelloBottomActions(
    onViewSourceCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onViewSourceCodeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.hello_view_source_code))
        }
    }
}
