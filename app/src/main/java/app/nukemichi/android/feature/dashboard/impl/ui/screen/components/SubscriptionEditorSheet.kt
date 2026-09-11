package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.nukemichi.android.R
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionEditorUi
import app.nukemichi.android.platform.ui.components.ConfirmDialog
import app.nukemichi.android.platform.ui.components.NukemichiTextField
import app.nukemichi.android.platform.ui.theme.size.dimens
import app.nukemichi.android.platform.ui.util.UiText

private const val MAX_NAME_LENGTH = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubscriptionEditorSheet(
    editor: SubscriptionEditorUi,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val dimens = MaterialTheme.dimens
    var name by rememberSaveable(editor.id) { mutableStateOf(editor.name) }
    var showDeleteConfirm by rememberSaveable(editor.id) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimens.l, end = dimens.l, bottom = dimens.l),
            verticalArrangement = Arrangement.spacedBy(dimens.l),
        ) {
            Text(
                text = stringResource(R.string.dashboard_subscription_edit_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            NukemichiTextField(
                value = name,
                onValueChange = { name = it.take(MAX_NAME_LENGTH) },
                modifier = Modifier.fillMaxWidth(),
                label = UiText.Resource(R.string.dashboard_subscription_name_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )

            Button(
                onClick = { onRename(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.dashboard_subscription_save))
            }

            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(text = stringResource(R.string.dashboard_subscription_delete))
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = UiText.Resource(R.string.dashboard_subscription_delete_confirm_title),
            body = UiText.Resource(R.string.dashboard_subscription_delete_confirm_body),
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
