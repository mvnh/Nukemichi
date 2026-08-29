package app.nukemichi.android.feature.wizard.impl.domain.usecase

import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.ext.execute
import app.nukemichi.android.core.vpn.configfactory.XrayServerConfigFactory
import app.nukemichi.android.feature.wizard.impl.domain.deployment.XrayOpenRcServiceFactory
import app.nukemichi.android.feature.wizard.impl.domain.deployment.XraySystemdServiceFactory
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentStep
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager
import app.nukemichi.android.feature.wizard.impl.domain.model.SniSelector
import app.nukemichi.android.feature.wizard.impl.domain.model.XrayServerCredentials
import app.nukemichi.android.feature.wizard.impl.domain.model.XrayServerSecrets
import app.nukemichi.android.feature.wizard.impl.domain.ssh.DetectPackageManagerCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.GenerateXrayServerSecretsCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.InstallXrayRuntimeCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.ScanSniCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.StartXrayServiceCommand
import app.nukemichi.android.feature.wizard.impl.domain.ssh.VerifySniCandidateCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class DeployXrayServerUseCase @Inject constructor() {

    operator fun invoke(connection: SshConnection, architecture: String): Flow<DeploymentEvent> =
        flow {
            val packageManager = runStep(DeploymentStep.INSTALL_RUNTIME) {
                runCatching {
                    val packageManager = connection.execute(DetectPackageManagerCommand()).getOrThrow()
                    emit(DeploymentEvent.LogLine(DeploymentStep.INSTALL_RUNTIME, "Detected package manager: ${packageManager.token}"))

                    connection.execute(
                        InstallXrayRuntimeCommand(packageManager, InstallXrayRuntimeCommand.releaseAssetFor(architecture)),
                        onOutputLine = { line -> emit(DeploymentEvent.LogLine(DeploymentStep.INSTALL_RUNTIME, line)) },
                    ).getOrThrow()
                    packageManager
                }
            } ?: return@flow

            val sni = runStep(DeploymentStep.FIND_SNI) {
                runCatching {
                    val candidates = connection.execute(ScanSniCommand(architecture)).getOrThrow()
                    emit(DeploymentEvent.LogLine(DeploymentStep.FIND_SNI, "Found ${candidates.size} candidate(s) nearby"))
                    findStableCandidate(connection, candidates)
                        ?: error("No suitable REALITY SNI found near this VPS.")
                }
            } ?: return@flow

            val secrets = runStep(DeploymentStep.GENERATE_SECRETS) {
                emit(DeploymentEvent.LogLine(DeploymentStep.GENERATE_SECRETS, "Generating uuid and x25519 keypair..."))
                connection.execute(GenerateXrayServerSecretsCommand())
            } ?: return@flow

            val credentials = runStep(DeploymentStep.WRITE_CONFIGURATION) {
                runCatching { uploadConfiguration(connection, secrets, sni, packageManager) }
            } ?: return@flow

            runStep(DeploymentStep.START_SERVICE) {
                connection.execute(
                    StartXrayServiceCommand(packageManager),
                    onOutputLine = { line ->
                        emit(
                            DeploymentEvent.LogLine(
                                DeploymentStep.START_SERVICE,
                                line
                            )
                        )
                    },
                )
            } ?: return@flow

            emit(DeploymentEvent.Completed(credentials))
        }

    private suspend fun FlowCollector<DeploymentEvent>.findStableCandidate(
        connection: SshConnection,
        candidates: List<String>,
    ): String? {
        val allowed = candidates.filter(SniSelector::isAllowed).shuffled()
        for (candidate in allowed.take(MAX_SNI_VERIFICATION_ATTEMPTS)) {
            val isStable = connection.execute(VerifySniCandidateCommand(candidate)).getOrDefault(false)
            if (isStable) return candidate
            emit(DeploymentEvent.LogLine(DeploymentStep.FIND_SNI, "$candidate failed a stability check, trying another candidate"))
        }
        return null
    }

    private suspend fun FlowCollector<DeploymentEvent>.uploadConfiguration(
        connection: SshConnection,
        secrets: XrayServerSecrets,
        realityServerName: String,
        packageManager: PackageManager,
    ): XrayServerCredentials {
        val serverConfigJson = XrayServerConfigFactory.build(
            uuid = secrets.uuid,
            privateKey = secrets.privateKey,
            shortId = secrets.shortId,
            realityServerName = realityServerName,
        ).toJson()
        emit(
            DeploymentEvent.LogLine(
                DeploymentStep.WRITE_CONFIGURATION,
                "Uploading /usr/local/etc/xray/config.json"
            )
        )
        connection.upload(
            CONFIG_PATH,
            serverConfigJson.toByteArray(),
            permissions = CONFIG_PERMISSIONS
        ).getOrThrow()

        if (packageManager == PackageManager.APK) {
            emit(DeploymentEvent.LogLine(DeploymentStep.WRITE_CONFIGURATION, "Uploading OpenRC service"))
            connection.upload(
                OPENRC_SERVICE_PATH,
                XrayOpenRcServiceFactory.create().toByteArray(),
                permissions = OPENRC_SERVICE_PERMISSIONS,
            ).getOrThrow()
        } else {
            emit(DeploymentEvent.LogLine(DeploymentStep.WRITE_CONFIGURATION, "Uploading systemd unit"))
            connection.upload(SYSTEMD_UNIT_PATH, XraySystemdServiceFactory.create().toByteArray())
                .getOrThrow()
        }

        return XrayServerCredentials(
            uuid = secrets.uuid,
            publicKey = secrets.publicKey,
            shortId = secrets.shortId,
            realityServerName = realityServerName,
        )
    }

    private suspend fun <T> FlowCollector<DeploymentEvent>.runStep(
        step: DeploymentStep,
        block: suspend () -> Result<T>,
    ): T? {
        emit(DeploymentEvent.StepStarted(step))
        return block().fold(
            onSuccess = { value -> emit(DeploymentEvent.StepSucceeded(step)); value },
            onFailure = { error -> emit(DeploymentEvent.StepFailed(step, error)); null },
        )
    }

    private companion object {
        const val CONFIG_PATH = "/usr/local/etc/xray/config.json"
        const val SYSTEMD_UNIT_PATH = "/etc/systemd/system/nukemichi-xray.service"
        const val OPENRC_SERVICE_PATH = "/etc/init.d/nukemichi-xray"
        const val OPENRC_SERVICE_PERMISSIONS = 0b111_101_101
        const val CONFIG_PERMISSIONS = 0b110_000_000 // rw------- (0600)
        const val MAX_SNI_VERIFICATION_ATTEMPTS = 5
    }
}
