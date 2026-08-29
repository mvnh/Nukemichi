package app.nukemichi.android.feature.wizard.impl.domain.model

/** Generated on the target VPS. [privateKey] is only used to build the server config and is never persisted or logged. */
internal data class XrayServerSecrets(
    val uuid: String,
    val privateKey: String,
    val publicKey: String,
    val shortId: String,
)
