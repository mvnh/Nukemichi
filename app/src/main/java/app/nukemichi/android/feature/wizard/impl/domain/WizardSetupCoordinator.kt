package app.nukemichi.android.feature.wizard.impl.domain

import app.nukemichi.android.core.ssh.SshManager
import app.nukemichi.android.core.ssh.ext.execute
import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig
import app.nukemichi.android.core.vpn.XraySubscription
import app.nukemichi.android.core.vpn.XraySubscriptionStore
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.configfactory.XrayServerConfigFactory
import app.nukemichi.android.core.vpn.findServerByAddress
import app.nukemichi.android.core.vpn.generateNickname
import app.nukemichi.android.core.vpn.newXrayId
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.withDeployedServer
import app.nukemichi.android.feature.wizard.impl.domain.model.DeploymentEvent
import app.nukemichi.android.feature.wizard.impl.domain.model.WizardProfileDraft
import app.nukemichi.android.feature.wizard.impl.domain.ssh.DetectServerArchCommand
import app.nukemichi.android.feature.wizard.impl.domain.usecase.DeployXrayServerUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class WizardSetupCoordinator @Inject constructor(
    private val sshManager: SshManager,
    private val subscriptionStore: XraySubscriptionStore,
    private val deployXrayServerUseCase: DeployXrayServerUseCase,
) {
    suspend fun validateConnection(sshConfig: SshConfig, auth: SshAuth): Result<String> =
        sshManager.withConnection(sshConfig, auth) { connection ->
            connection.execute(DetectServerArchCommand())
        }

    fun deploy(sshConfig: SshConfig, auth: SshAuth, architecture: String): Flow<DeploymentEvent> = flow {
        sshManager.withConnection(sshConfig, auth) { connection ->
            deployXrayServerUseCase(connection, architecture).collect { event -> emit(event) }
            Result.success(Unit)
        }.onFailure { error -> emit(DeploymentEvent.Failed(error)) }
    }

    /**
     * Stores the deployed server in [targetSubscriptionId], or in a new randomly named subscription, and
     * returns the stored entry. A redeploy to a known address keeps that entry's id, so callers must use
     * the returned profile rather than anything derived from the draft.
     */
    suspend fun saveProfile(draft: WizardProfileDraft, targetSubscriptionId: String?): Result<XrayVpnProfile> {
        if (!draft.isDeployable) return Result.failure(IllegalArgumentException("Server-side Xray credentials are missing."))

        val deployed = XrayVpnProfile(
            id = newXrayId(),
            name = generateNickname(draft.serverAddress),
            sshHost = draft.serverAddress,
            sshPort = draft.sshPort,
            sshUsername = draft.sshUsername,
            sshExpectedFingerprint = draft.sshExpectedFingerprint,
            serverAddress = draft.serverAddress,
            serverPort = XrayServerConfigFactory.DEFAULT_SERVER_PORT,
            uuid = draft.uuid,
            security = XraySecurity.Reality(
                serverName = draft.realityServerName,
                publicKey = draft.realityPublicKey,
                shortId = draft.realityShortId,
            ),
            deployedAtMillis = System.currentTimeMillis(),
            countryCode = draft.countryCode,
        )
        return try {
            val stored = subscriptionStore.update { subscriptions ->
                subscriptions.withDeployedServer(deployed, targetSubscriptionId) {
                    val subscriptionId = newXrayId()
                    XraySubscription(id = subscriptionId, name = generateNickname(subscriptionId), servers = emptyList())
                }
            }
            Result.success(requireNotNull(stored.findServerByAddress(deployed.serverAddress)))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private val WizardProfileDraft.isDeployable: Boolean
        get() = UUID_PATTERN.matches(uuid) &&
            realityPublicKey.isNotBlank() && realityShortId.isNotBlank() && realityServerName.isNotBlank()

    private companion object {
        val UUID_PATTERN = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
        )
    }
}
