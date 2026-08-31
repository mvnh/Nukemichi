package app.nukemichi.android.feature.wizard.impl.ui.mvi

import app.nukemichi.android.core.security.Secret
import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig
import app.nukemichi.android.core.ui.util.UiSecret
import app.nukemichi.android.feature.wizard.impl.domain.model.WizardProfileDraft

internal fun WizardContract.State.toSshConfigOrNull(): SshConfig? =
    sshPort.toIntOrNull()?.takeIf { it in 1..65_535 }?.let { port ->
        serverAddress.trim().takeIf(String::isNotEmpty)?.let { host ->
            username.trim().takeIf(String::isNotEmpty)?.let { user ->
                SshConfig(host = host, port = port, username = user, expectedFingerprint = sshFingerprint.trim().ifBlank { null })
            }
        }
    }

private fun UiSecret.toSecret(): Secret = Secret(value)

internal fun WizardContract.State.toSshAuth(): SshAuth = when (serverAuthMethod) {
    WizardContract.ServerAuthMethod.PASSWORD -> SshAuth.Password(password.toSecret())
    WizardContract.ServerAuthMethod.SSH_KEY -> SshAuth.PrivateKey(sshKey.toSecret())
}

internal fun WizardContract.State.toProfileDraft(): WizardProfileDraft = WizardProfileDraft(
    serverAddress = serverAddress.trim(),
    sshPort = sshPort.toInt(),
    sshUsername = username.trim(),
    sshExpectedFingerprint = sshFingerprint.trim().ifBlank { null },
    uuid = uuid.trim(),
    realityServerName = realityServerName.trim(),
    realityPublicKey = realityPublicKey.trim(),
    realityShortId = realityShortId.trim(),
)
