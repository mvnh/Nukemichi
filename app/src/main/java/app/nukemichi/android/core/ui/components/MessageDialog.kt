package app.nukemichi.android.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString

@Composable
fun MessageDialog(
    title: UiText,
    body: UiText,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    confirmText: UiText = UiText.Resource(R.string.ok),
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        modifier = modifier,
        icon = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        title = { Text(text = title.asString()) },
        text = { Text(text = body.asString(), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText.asString())
            }
        },
    )
}
