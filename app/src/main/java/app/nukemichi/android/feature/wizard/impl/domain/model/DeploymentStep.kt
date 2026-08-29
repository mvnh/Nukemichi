package app.nukemichi.android.feature.wizard.impl.domain.model

internal enum class DeploymentStep {
    INSTALL_RUNTIME,
    FIND_SNI,
    GENERATE_SECRETS,
    WRITE_CONFIGURATION,
    START_SERVICE,
}
