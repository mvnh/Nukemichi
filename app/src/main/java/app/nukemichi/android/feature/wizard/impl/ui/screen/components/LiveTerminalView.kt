package app.nukemichi.android.feature.wizard.impl.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.components.MonospaceLogList
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LiveTerminalView(
    logLines: ImmutableList<String>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Column(modifier = modifier) {
        TextButton(onClick = onToggleExpanded) {
            Icon(
                imageVector = if (isExpanded) NukemichiIcons.Navigation.ArrowDropUp else NukemichiIcons.Navigation.ArrowDropDown,
                contentDescription = null,
            )
            Text(
                text = stringResource(
                    if (isExpanded) R.string.wizard_deployment_terminal_hide else R.string.wizard_deployment_terminal_show
                )
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                MonospaceLogList(
                    lines = logLines,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = TERMINAL_MAX_HEIGHT),
                    contentPadding = PaddingValues(dimens.m),
                )
            }
        }
    }
}

private val TERMINAL_MAX_HEIGHT = 240.dp
