package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import android.content.Intent as SystemIntent
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayTrafficStats
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.feature.dashboard.impl.ui.model.ServerDetailsUi
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionEditorUi
import app.nukemichi.android.feature.dashboard.impl.ui.model.SubscriptionUi
import app.nukemichi.android.platform.ui.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object DashboardContract {

    data class State(
        val engineState: XrayEngineState = XrayEngineState.IDLE,
        val stats: XrayTrafficStats? = null,
        val connectedSinceRealtime: Long? = null,
        val errorMessage: UiText? = null,
        val isLibraryLoaded: Boolean = false,
        val subscriptions: ImmutableList<SubscriptionUi> = persistentListOf(),
        val serverDetails: ServerDetailsUi? = null,
        val subscriptionEditor: SubscriptionEditorUi? = null,
        val isAdvancedMode: Boolean = false,
    )

    sealed interface Intent {
        data object ToggleConnection : Intent
        data object VpnPermissionGranted : Intent
        data object VpnPermissionDenied : Intent
        data object ErrorDismissed : Intent

        data class ServerSelected(val serverId: String) : Intent
        data class SubscriptionExpandToggled(val subscriptionId: String) : Intent
        /** A null [subscriptionId] deploys into a new subscription. */
        data class DeployServerRequested(val subscriptionId: String?) : Intent
        data class ShareSubscriptionRequested(val subscriptionId: String) : Intent

        data class SubscriptionEditorRequested(val subscriptionId: String) : Intent
        data object SubscriptionEditorDismissed : Intent
        data class SubscriptionRenamed(val subscriptionId: String, val name: String) : Intent
        data class SubscriptionDeleted(val subscriptionId: String) : Intent

        data class ServerDetailsRequested(val serverId: String) : Intent
        data object ServerDetailsDismissed : Intent
        data class ShareServerRequested(val serverId: String) : Intent
        data class ServerForgotten(val serverId: String) : Intent
        data class FingerprintChanged(val serverId: String, val value: XrayFingerprint) : Intent
        data class MuxEnabledChanged(val serverId: String, val enabled: Boolean) : Intent
        data class MuxConcurrencyChanged(val serverId: String, val value: Int) : Intent
    }

    sealed interface Effect {
        data class RequestVpnPermission(val permissionIntent: SystemIntent) : Effect
        data class ShareText(val text: String) : Effect
        data class NavigateToWizard(val subscriptionId: String?) : Effect
    }
}

internal val DashboardContract.State.isConnected: Boolean
    get() = engineState == XrayEngineState.RUNNING

internal val DashboardContract.State.isBusy: Boolean
    get() = engineState == XrayEngineState.STARTING || engineState == XrayEngineState.STOPPING
