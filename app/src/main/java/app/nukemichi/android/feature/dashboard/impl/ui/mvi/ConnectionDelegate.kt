package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import android.content.Context
import android.net.VpnService
import app.nukemichi.android.R
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayServiceProvider
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.feature.dashboard.impl.domain.ServerLibraryCoordinator
import app.nukemichi.android.platform.ui.mvi.ViewModelDelegate
import app.nukemichi.android.platform.ui.util.UiText
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

internal class ConnectionDelegate @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val serviceProvider: XrayServiceProvider,
    private val coordinator: ServerLibraryCoordinator,
) : ViewModelDelegate<DashboardContract.State, DashboardContract.Effect>() {

    // Guards against re-entering handleHealthDegraded(): its own reconnect passes through the same
    // STOPPING/STOPPED/IDLE states as a manual disconnect, with nothing else to tell them apart.
    private var autoReconnecting = false

    // From :vpn rather than remembered here: a UI process recreated underneath a live tunnel would forget it.
    private var sessionServerId: String? = null

    fun observe() {
        // Subscribed alongside state, before the IPC bind completes, so the replay a new client gets on
        // registering reaches both.
        serviceProvider.monitoring.sessionServerId
            .onEach { serverId ->
                sessionServerId = serverId
                // A highlight that disagrees with where traffic actually goes is worse than one the user did not pick.
                serverId?.let(coordinator::select)
            }
            .launchIn(scope)
        serviceProvider.monitoring.healthDegraded
            .onEach { handleHealthDegraded() }
            .launchIn(scope)
        serviceProvider.monitoring.state
            .onEach { engineState -> reduce { copy(engineState = engineState) } }
            .launchIn(scope)
        // From :vpn rather than derived from `state` here: a UI process recreated underneath a
        // live tunnel would otherwise see RUNNING for the first time on reconnect and mistake that
        // moment for the start of the session, resetting "connected for" to zero.
        serviceProvider.monitoring.runningSinceRealtime
            .onEach { runningSinceRealtime -> reduce { copy(connectedSinceRealtime = runningSinceRealtime) } }
            .launchIn(scope)
        serviceProvider.monitoring.stats
            .onEach { stats -> reduce { copy(stats = stats) } }
            .launchIn(scope)
    }

    /** Confirmation of start/stop arrives later over IPC, leaving a window (widest on a cold
     *  `:vpn` process spawn) where a repeat tap would dispatch a second START/STOP that Android's
     *  VPN subsystem doesn't tolerate back-to-back. [DashboardContract.State.engineState] flips to
     *  STARTING/STOPPING synchronously here, before the IPC round trip, so a queued repeat tap no-ops on `isBusy`. */
    suspend fun toggle() {
        if (currentState.isBusy) return

        if (currentState.engineState == XrayEngineState.RUNNING) {
            stop()
            return
        }
        val permissionIntent = VpnService.prepare(appContext)
        if (permissionIntent != null) {
            sendEffect(DashboardContract.Effect.RequestVpnPermission(permissionIntent))
        } else {
            beginStarting()
        }
    }

    suspend fun onPermissionGranted() = beginStarting()

    fun onPermissionDenied() {
        reduce { copy(errorMessage = UiText.Resource(R.string.dashboard_error_vpn_permission_denied)) }
    }

    /** A stopped engine picks the selection up on its next start instead. */
    suspend fun followSelection() {
        if (currentState.engineState != XrayEngineState.RUNNING) return
        val server = coordinator.selectedServer() ?: return
        if (server.id == sessionServerId) return
        reduce { copy(engineState = XrayEngineState.STARTING, errorMessage = null) }
        val dispatched = start(server, stateOnDispatchFailure = XrayEngineState.RUNNING)
        // Nothing reached the service, so the old session keeps running: put the selection back on it.
        if (!dispatched) sessionServerId?.let(coordinator::select)
    }

    suspend fun stopIfRunningOn(removedServerIds: Set<String>) {
        if (currentState.engineState != XrayEngineState.RUNNING) return
        if (sessionServerId?.let(removedServerIds::contains) == true) stop()
    }

    private suspend fun stop() {
        reduce { copy(engineState = XrayEngineState.STOPPING, errorMessage = null) }
        serviceProvider.control.stop().onFailure { error ->
            // Dispatch failed synchronously, so nothing arrives over IPC to undo the
            // optimistic state above and this has to do it instead.
            Timber.e(error, "stop() dispatch failed")
            reduce {
                copy(
                    engineState = XrayEngineState.RUNNING,
                    errorMessage = UiText.Resource(R.string.dashboard_error_failed_to_stop_vpn),
                )
            }
        }
    }

    private suspend fun beginStarting() {
        if (currentState.isBusy) return
        reduce { copy(engineState = XrayEngineState.STARTING, errorMessage = null) }
        val server = coordinator.selectedServer()
        if (server == null) {
            reduce {
                copy(
                    engineState = XrayEngineState.IDLE,
                    errorMessage = UiText.Resource(R.string.dashboard_error_no_server_configured),
                )
            }
            return
        }
        start(server, stateOnDispatchFailure = XrayEngineState.IDLE)
    }

    /** Only whether the start reached the service; the session itself is confirmed later over IPC. */
    private suspend fun start(server: XrayVpnProfile, stateOnDispatchFailure: XrayEngineState): Boolean =
        serviceProvider.control.start(XrayClientConfigFactory.createRuntimeConfig(server))
            .onFailure { error ->
                Timber.e(error, "start() dispatch failed")
                reduce {
                    copy(
                        engineState = stateOnDispatchFailure,
                        errorMessage = UiText.Resource(R.string.dashboard_error_failed_to_start_vpn),
                    )
                }
            }
            .isSuccess

    /**
     * The `:vpn` process has already stopped itself and is about to be killed and respawned (see
     * `NukemichiVpnService.onHealthDegraded`), so this only waits for [XrayEngineState.IDLE]
     * before starting a new session.
     */
    private suspend fun handleHealthDegraded() {
        if (autoReconnecting) return
        autoReconnecting = true
        try {
            reduce { copy(errorMessage = UiText.Resource(R.string.dashboard_error_connection_stalled)) }
            val wentIdle = withTimeoutOrNull(IDLE_WAIT_TIMEOUT_MS) {
                serviceProvider.monitoring.state.first { it == XrayEngineState.IDLE }
            }
            if (wentIdle == null) {
                reduce {
                    copy(
                        engineState = XrayEngineState.ERROR,
                        errorMessage = UiText.Resource(R.string.dashboard_error_reconnect_timed_out),
                    )
                }
                return
            }
            beginStarting()
        } finally {
            autoReconnecting = false
        }
    }

    private companion object {
        const val IDLE_WAIT_TIMEOUT_MS = 15_000L
    }
}
