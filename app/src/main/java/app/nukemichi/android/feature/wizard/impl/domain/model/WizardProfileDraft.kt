package app.nukemichi.android.feature.wizard.impl.domain.model

internal data class WizardProfileDraft(
    val serverAddress: String,
    val sshPort: Int,
    val sshUsername: String,
    val sshExpectedFingerprint: String?,
    val uuid: String,
    val realityServerName: String,
    val realityPublicKey: String,
    val realityShortId: String,
)
