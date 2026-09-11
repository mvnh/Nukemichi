package app.nukemichi.android.feature.dashboard.impl.domain

import app.nukemichi.android.core.vpn.XraySubscriptionStore
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.findServer
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.withServerUpdated
import app.nukemichi.android.core.vpn.withSubscriptionUpdated
import app.nukemichi.android.core.vpn.withoutServer
import app.nukemichi.android.core.vpn.withoutSubscription
import app.nukemichi.android.feature.dashboard.impl.domain.model.ServerLibrary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

internal class ServerLibraryCoordinator @Inject constructor(
    private val subscriptionStore: XraySubscriptionStore,
    private val preferences: ServerListPreferencesRepository,
) {
    val library: Flow<ServerLibrary> =
        combine(subscriptionStore.subscriptions, preferences.preferences) { subscriptions, preferences ->
            ServerLibrary(
                subscriptions = subscriptions,
                // A selection whose server is gone falls back to the first server, never to nothing.
                selectedServer = preferences.selectedServerId?.let(subscriptions::findServer)
                    ?: subscriptions.firstNotNullOfOrNull { it.servers.firstOrNull() },
                collapsedSubscriptionIds = preferences.collapsedSubscriptionIds,
            )
        }

    suspend fun selectedServer(): XrayVpnProfile? = library.first().selectedServer

    fun select(serverId: String) = preferences.select(serverId)

    fun setCollapsed(subscriptionId: String, collapsed: Boolean) = preferences.setCollapsed(subscriptionId, collapsed)

    suspend fun renameSubscription(subscriptionId: String, name: String) {
        val trimmed = name.trim().takeIf(String::isNotEmpty) ?: return
        subscriptionStore.update { subscriptions ->
            subscriptions.withSubscriptionUpdated(subscriptionId) { it.copy(name = trimmed) }
        }
    }

    suspend fun deleteSubscription(subscriptionId: String) {
        subscriptionStore.update { it.withoutSubscription(subscriptionId) }
        preferences.setCollapsed(subscriptionId, collapsed = false)
    }

    suspend fun forgetServer(serverId: String) {
        subscriptionStore.update { it.withoutServer(serverId) }
    }

    suspend fun setFingerprint(serverId: String, fingerprint: XrayFingerprint) = updateServer(serverId) { server ->
        val reality = server.security as? XraySecurity.Reality ?: return@updateServer server
        server.copy(security = reality.copy(fingerprint = fingerprint))
    }

    suspend fun setMuxEnabled(serverId: String, enabled: Boolean) = updateServer(serverId) { it.copy(muxEnabled = enabled) }

    suspend fun setMuxConcurrency(serverId: String, concurrency: Int) =
        updateServer(serverId) { it.copy(muxConcurrency = concurrency) }

    private suspend fun updateServer(serverId: String, transform: (XrayVpnProfile) -> XrayVpnProfile) {
        subscriptionStore.update { it.withServerUpdated(serverId, transform) }
    }
}
