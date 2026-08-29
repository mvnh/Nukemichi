package app.nukemichi.android.feature.wizard.impl.domain.model

internal data class XrayServerCredentials(
    val uuid: String,
    val publicKey: String,
    val shortId: String,
    val realityServerName: String,
)
