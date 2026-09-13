package app.nukemichi.android.feature.hello.impl.ui.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.nukemichi.android.R

// Settings, and Advanced mode inside it, is deliberately not linked from here: with no server set
// up there is nothing in it to see, and surfacing "advanced mode" before someone has connected once
// reads as a hidden, more-powerful option. Reachable from Dashboard once a server exists.
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
