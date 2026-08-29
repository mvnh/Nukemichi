package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import android.content.Intent as SystemIntent
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayTrafficStats

internal object DashboardContract {

    data class State(
        val profileName: String? = null,
        val serverAddress: String? = null,
        val realityServerName: String? = null,
        val deployedAtMillis: Long? = null,
        val engineState: XrayEngineState = XrayEngineState.IDLE,
        val stats: XrayTrafficStats? = null,
        val connectedSinceMillis: Long? = null,
        val errorMessage: UiText? = null,
    )

    sealed interface Intent {
        data object ToggleConnection : Intent
        data object VpnPermissionGranted : Intent
        data object VpnPermissionDenied : Intent
        data object ErrorDismissed : Intent
        data object ExportVlessLinkRequested : Intent
    }

    sealed interface Effect {
        data class RequestVpnPermission(val permissionIntent: SystemIntent) : Effect
        data class ShareVlessLink(val uri: String) : Effect
    }
}

internal val DashboardContract.State.isConnected: Boolean
    get() = engineState == XrayEngineState.RUNNING

internal val DashboardContract.State.isBusy: Boolean
    get() = engineState == XrayEngineState.STARTING || engineState == XrayEngineState.STOPPING
