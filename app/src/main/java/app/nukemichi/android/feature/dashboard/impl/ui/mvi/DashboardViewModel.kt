package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import androidx.compose.runtime.Stable
import app.nukemichi.android.feature.dashboard.impl.domain.ServerLibraryCoordinator
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract.Effect
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract.Intent
import app.nukemichi.android.feature.dashboard.impl.ui.mvi.DashboardContract.State
import app.nukemichi.android.platform.mode.AppMode
import app.nukemichi.android.platform.mode.AppModeRepository
import app.nukemichi.android.platform.ui.mvi.MviViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
@HiltViewModel(assistedFactory = DashboardViewModel.Factory::class)
internal class DashboardViewModel @AssistedInject constructor(
    @Assisted selectServerId: String?,
    private val coordinator: ServerLibraryCoordinator,
    appModeRepository: AppModeRepository,
    private val connectionDelegate: ConnectionDelegate,
    private val libraryDelegate: ServerLibraryDelegate,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        attachDelegates(connectionDelegate, libraryDelegate)
        // Selection only: whoever handed this server over has already started any session on it.
        selectServerId?.let(coordinator::select)
        connectionDelegate.observe()
        libraryDelegate.observe()
        appModeRepository.mode
            .onEach { mode -> reduce { copy(isAdvancedMode = mode == AppMode.ADVANCED) } }
            .launchIn(scope)
    }

    override suspend fun onIntent(intent: Intent) {
        when (intent) {
            Intent.ToggleConnection -> connectionDelegate.toggle()
            Intent.VpnPermissionGranted -> connectionDelegate.onPermissionGranted()
            Intent.VpnPermissionDenied -> connectionDelegate.onPermissionDenied()
            Intent.ErrorDismissed -> reduce { copy(errorMessage = null) }

            is Intent.ServerSelected -> if (libraryDelegate.select(intent.serverId)) connectionDelegate.followSelection()
            is Intent.SubscriptionExpandToggled -> libraryDelegate.toggleExpanded(intent.subscriptionId)
            is Intent.DeployServerRequested -> sendEffect(Effect.NavigateToWizard(intent.subscriptionId))
            is Intent.ShareSubscriptionRequested -> libraryDelegate.shareSubscription(intent.subscriptionId)

            is Intent.SubscriptionEditorRequested -> libraryDelegate.openSubscriptionEditor(intent.subscriptionId)
            Intent.SubscriptionEditorDismissed -> libraryDelegate.dismissSubscriptionEditor()
            is Intent.SubscriptionRenamed -> libraryDelegate.renameSubscription(intent.subscriptionId, intent.name)
            is Intent.SubscriptionDeleted -> {
                connectionDelegate.stopIfRunningOn(libraryDelegate.serverIdsOf(intent.subscriptionId))
                libraryDelegate.deleteSubscription(intent.subscriptionId)
            }

            is Intent.ServerDetailsRequested -> libraryDelegate.openServerDetails(intent.serverId)
            Intent.ServerDetailsDismissed -> libraryDelegate.dismissServerDetails()
            is Intent.ShareServerRequested -> libraryDelegate.shareServer(intent.serverId)
            is Intent.ServerForgotten -> {
                connectionDelegate.stopIfRunningOn(setOf(intent.serverId))
                libraryDelegate.forgetServer(intent.serverId)
            }
            is Intent.FingerprintChanged -> coordinator.setFingerprint(intent.serverId, intent.value)
            is Intent.MuxEnabledChanged -> coordinator.setMuxEnabled(intent.serverId, intent.enabled)
            is Intent.MuxConcurrencyChanged -> coordinator.setMuxConcurrency(intent.serverId, intent.value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(selectServerId: String?): DashboardViewModel
    }
}
