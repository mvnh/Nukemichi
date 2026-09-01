package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import android.content.Context
import android.net.VpnService
import android.os.SystemClock
import androidx.compose.runtime.Stable
import app.nukemichi.android.R
import app.nukemichi.android.core.ui.mvi.MviViewModel
import app.nukemichi.android.core.ui.util.UiText
import app.nukemichi.android.core.vpn.configfactory.XrayClientConfigFactory
import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayProfileStore
import app.nukemichi.android.core.vpn.XrayServiceProvider
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.toVlessUri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

@Stable
@HiltViewModel
internal class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileStore: XrayProfileStore,
    private val serviceProvider: XrayServiceProvider,
) : MviViewModel<DashboardContract.State, DashboardContract.Intent, DashboardContract.Effect>(
    profileStore.getActiveProfile().let { profile ->
        DashboardContract.State(
            profileName = profile?.name,
            serverAddress = profile?.serverAddress,
            realityServerName = (profile?.security as? XraySecurity.Reality)?.serverName,
            deployedAtMillis = profile?.deployedAtMillis,
        )
    }
) {

    // Guards against re-entering handleHealthDegraded(): its own reconnect passes through the same
    // STOPPING/STOPPED/IDLE states as a manual disconnect, with nothing else to tell them apart.
    private var autoReconnecting = false

    init {
        serviceProvider.monitoring.healthDegraded
            .onEach { handleHealthDegraded() }
            .launchIn(scope)
        serviceProvider.monitoring.state
            .onEach { engineState ->
                reduce {
                    copy(
                        engineState = engineState,
                        // Set once entering RUNNING and cleared on any exit. Recomputing it on
                        // every stats tick would reset "connected for" to zero each time.
                        connectedSinceRealtime = when {
                            engineState == XrayEngineState.RUNNING && connectedSinceRealtime == null ->
                                SystemClock.elapsedRealtime()
                            engineState != XrayEngineState.RUNNING -> null
                            else -> connectedSinceRealtime
                        },
                    )
                }
            }
            .launchIn(scope)
        serviceProvider.monitoring.stats
            .onEach { stats -> reduce { copy(stats = stats) } }
            .launchIn(scope)
    }

    override suspend fun onIntent(intent: DashboardContract.Intent) {
        when (intent) {
            DashboardContract.Intent.ToggleConnection -> toggleConnection()
            DashboardContract.Intent.VpnPermissionGranted -> beginStartingVpn()
            DashboardContract.Intent.VpnPermissionDenied ->
                reduce { copy(errorMessage = UiText.Resource(R.string.dashboard_error_vpn_permission_denied)) }
            DashboardContract.Intent.ErrorDismissed -> reduce { copy(errorMessage = null) }
            DashboardContract.Intent.ExportVlessLinkRequested -> exportVlessLink()
        }
    }

    private fun exportVlessLink() {
        profileStore.getActiveProfile()?.let { profile ->
            sendEffect(DashboardContract.Effect.ShareVlessLink(profile.toVlessUri()))
        }
    }

    /** Confirmation of start/stop arrives later over IPC, leaving a window (widest on a cold
     *  `:vpn` process spawn) where a repeat tap would dispatch a second START/STOP that Android's
     *  VPN subsystem doesn't tolerate back-to-back. [state] flips to STARTING/STOPPING
     *  synchronously here, before the IPC round trip, so a queued repeat tap no-ops on `isBusy`. */
    private suspend fun toggleConnection() {
        if (state.value.isBusy) return

        when (state.value.engineState) {
            XrayEngineState.RUNNING -> {
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

            else -> {
                val permissionIntent = VpnService.prepare(context)
                if (permissionIntent != null) {
                    sendEffect(DashboardContract.Effect.RequestVpnPermission(permissionIntent))
                } else {
                    beginStartingVpn()
                }
            }
        }
    }

    private suspend fun beginStartingVpn() {
        if (state.value.isBusy) return
        reduce { copy(engineState = XrayEngineState.STARTING, errorMessage = null) }
        startVpn()
    }

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
            beginStartingVpn()
        } finally {
            autoReconnecting = false
        }
    }

    private suspend fun startVpn() {
        val profile = profileStore.getActiveProfile()
        if (profile == null) {
            reduce {
                copy(
                    engineState = XrayEngineState.IDLE,
                    errorMessage = UiText.Resource(R.string.dashboard_error_no_server_configured),
                )
            }
            return
        }
        val config = XrayClientConfigFactory.createRuntimeConfig(profile)
        serviceProvider.control.start(config)
            .onFailure { error ->
                Timber.e(error, "start() dispatch failed")
                reduce {
                    copy(
                        engineState = XrayEngineState.IDLE,
                        errorMessage = UiText.Resource(R.string.dashboard_error_failed_to_start_vpn),
                    )
                }
            }
    }

    private companion object {
        const val IDLE_WAIT_TIMEOUT_MS = 15_000L
    }
}
