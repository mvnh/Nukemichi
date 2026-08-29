package app.nukemichi.android.feature.wizard.impl.ui.screen.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.components.NukemichiTextField
import app.nukemichi.android.core.ui.icons.NukemichiIcons
import app.nukemichi.android.core.ui.theme.size.dimens
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.ui.util.asString
import app.nukemichi.android.feature.wizard.impl.ui.screen.components.WizardPage
import app.nukemichi.android.feature.wizard.impl.ui.mvi.WizardContract.ServerAuthMethod

@Composable
fun ServerDataEntryPage(
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    authMethod: ServerAuthMethod,
    onAuthMethodChange: (ServerAuthMethod) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    sshKey: String,
    onSshKeyChange: (String) -> Unit,
    port: String,
    onPortChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    sshFingerprint: String,
    onSshFingerprintChange: (String) -> Unit,
    isAdvancedSheetOpen: Boolean,
    onAdvancedSheetDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    WizardPage(modifier = modifier) {
        Text(
            text = stringResource(R.string.wizard_server_data_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(dimens.l))

        SectionHeader(
            icon = NukemichiIcons.Common.Dns,
            title = UiText.Resource(R.string.wizard_server_data_connection_title)
        )

        Spacer(modifier = Modifier.height(dimens.m))

        NukemichiTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = UiText.Resource(R.string.wizard_server_data_address),
            placeholder = UiText.Raw("192.168.1.100"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        Spacer(modifier = Modifier.height(dimens.l))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(dimens.l))

        SectionHeader(
            icon = NukemichiIcons.Common.Lock,
            title = UiText.Resource(R.string.wizard_server_data_authentication)
        )

        Spacer(modifier = Modifier.height(dimens.m))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = authMethod == ServerAuthMethod.PASSWORD,
                onClick = { onAuthMethodChange(ServerAuthMethod.PASSWORD) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text(stringResource(R.string.wizard_server_data_password)) }
            )
            SegmentedButton(
                selected = authMethod == ServerAuthMethod.SSH_KEY,
                onClick = { onAuthMethodChange(ServerAuthMethod.SSH_KEY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text(stringResource(R.string.wizard_server_data_ssh_key)) }
            )
        }

        Spacer(modifier = Modifier.height(dimens.m))

        AnimatedContent(
            targetState = authMethod,
            label = "authentication_method"
        ) { method ->
            when (method) {
                ServerAuthMethod.PASSWORD -> {
                    NukemichiTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = UiText.Resource(R.string.wizard_server_data_password),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) {
                                        NukemichiIcons.Common.VisibilityOff
                                    } else {
                                        NukemichiIcons.Common.Visibility
                                    },
                                    contentDescription = null,
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }

                ServerAuthMethod.SSH_KEY -> {
                    NukemichiTextField(
                        value = sshKey,
                        onValueChange = onSshKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = UiText.Resource(R.string.wizard_server_data_ssh_key),
                        placeholder = UiText.Raw("-----BEGIN OPENSSH PRIVATE KEY-----"),
                        minLines = 6,
                        maxLines = 10,
                    )
                }
            }
        }
    }

    if (isAdvancedSheetOpen) {
        AdvancedSettingsSheet(
            port = port,
            onPortChange = onPortChange,
            username = username,
            onUsernameChange = onUsernameChange,
            sshFingerprint = sshFingerprint,
            onSshFingerprintChange = onSshFingerprintChange,
            onDismiss = onAdvancedSheetDismiss,
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: UiText,
    modifier: Modifier = Modifier,
) {
    val dimens = MaterialTheme.dimens

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.s)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title.asString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSettingsSheet(
    port: String,
    onPortChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    sshFingerprint: String,
    onSshFingerprintChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val dimens = MaterialTheme.dimens
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.l, vertical = dimens.l)
        ) {
            Text(
                text = stringResource(R.string.wizard_server_data_advanced_settings),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimens.l))

            NukemichiTextField(
                value = port,
                onValueChange = onPortChange,
                modifier = Modifier.fillMaxWidth(),
                label = UiText.Resource(R.string.wizard_server_data_port),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Spacer(modifier = Modifier.height(dimens.m))

            NukemichiTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                label = UiText.Resource(R.string.wizard_server_data_username),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(dimens.m))

            NukemichiTextField(
                value = sshFingerprint,
                onValueChange = onSshFingerprintChange,
                modifier = Modifier.fillMaxWidth(),
                label = UiText.Resource(R.string.wizard_server_data_fingerprint),
                singleLine = true,
            )
        }
    }
}
