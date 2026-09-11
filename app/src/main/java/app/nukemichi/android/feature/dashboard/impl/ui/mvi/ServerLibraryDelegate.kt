package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import app.nukemichi.android.core.vpn.findServer
import app.nukemichi.android.core.vpn.findSubscription
import app.nukemichi.android.core.vpn.toVlessUri
import app.nukemichi.android.core.vpn.toVlessUriList
import app.nukemichi.android.feature.dashboard.impl.domain.ServerLibraryCoordinator
import app.nukemichi.android.feature.dashboard.impl.domain.model.ServerLibrary
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionEditorUi
import app.nukemichi.android.platform.ui.mvi.ViewModelDelegate
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ServerLibraryDelegate @Inject constructor(
    private val coordinator: ServerLibraryCoordinator,
) : ViewModelDelegate<DashboardContract.State, DashboardContract.Effect>() {

    private var library: ServerLibrary? = null

    val selectedServerId: String?
        get() = library?.selectedServer?.id

    fun observe() {
        coordinator.library
            .onEach { latest ->
                library = latest
                reduce {
                    copy(
                        isLibraryLoaded = true,
                        subscriptions = latest.toSubscriptionsUi(),
                        // An open sheet follows edits to its subject and closes once that subject is gone.
                        serverDetails = serverDetails?.let { latest.subscriptions.findServer(it.id)?.toDetailsUi() },
                        subscriptionEditor = subscriptionEditor?.takeIf { latest.subscriptions.findSubscription(it.id) != null },
                    )
                }
            }
            .launchIn(scope)
    }

    /** @return whether the selection moved, and with it whether a running session has to follow. */
    fun select(serverId: String): Boolean {
        if (serverId == selectedServerId) return false
        coordinator.select(serverId)
        return true
    }

    fun toggleExpanded(subscriptionId: String) {
        val collapsed = library?.collapsedSubscriptionIds ?: return
        coordinator.setCollapsed(subscriptionId, collapsed = subscriptionId !in collapsed)
    }

    fun serverIdsOf(subscriptionId: String): Set<String> =
        library?.subscriptions?.findSubscription(subscriptionId)?.servers?.mapTo(mutableSetOf()) { it.id }.orEmpty()

    fun shareSubscription(subscriptionId: String) {
        library?.subscriptions?.findSubscription(subscriptionId)?.let { subscription ->
            sendEffect(DashboardContract.Effect.ShareText(subscription.toVlessUriList()))
        }
    }

    fun shareServer(serverId: String) {
        library?.subscriptions?.findServer(serverId)?.let { server ->
            sendEffect(DashboardContract.Effect.ShareText(server.toVlessUri()))
        }
    }

    fun openServerDetails(serverId: String) {
        val details = library?.subscriptions?.findServer(serverId)?.toDetailsUi() ?: return
        reduce { copy(serverDetails = details) }
    }

    fun dismissServerDetails() {
        reduce { copy(serverDetails = null) }
    }

    suspend fun forgetServer(serverId: String) {
        reduce { copy(serverDetails = null) }
        coordinator.forgetServer(serverId)
    }

    fun openSubscriptionEditor(subscriptionId: String) {
        val subscription = library?.subscriptions?.findSubscription(subscriptionId) ?: return
        reduce { copy(subscriptionEditor = SubscriptionEditorUi(id = subscription.id, name = subscription.name)) }
    }

    fun dismissSubscriptionEditor() {
        reduce { copy(subscriptionEditor = null) }
    }

    suspend fun renameSubscription(subscriptionId: String, name: String) {
        reduce { copy(subscriptionEditor = null) }
        coordinator.renameSubscription(subscriptionId, name)
    }

    suspend fun deleteSubscription(subscriptionId: String) {
        reduce { copy(subscriptionEditor = null) }
        coordinator.deleteSubscription(subscriptionId)
    }
}
