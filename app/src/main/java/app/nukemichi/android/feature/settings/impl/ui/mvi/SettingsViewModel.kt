package app.nukemichi.android.feature.settings.impl.ui.mvi

import androidx.compose.runtime.Stable
import app.nukemichi.android.core.mode.AppModeRepository
import app.nukemichi.android.core.ui.mvi.PatternViewModel
import app.nukemichi.android.core.vpn.XrayProfileStore
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.core.vpn.spec.XraySecurity
import app.nukemichi.android.core.vpn.spec.XrayTransport
import app.nukemichi.android.core.vpn.toVlessUri
import app.nukemichi.android.feature.settings.impl.domain.usecase.UpdateXrayTransportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@Stable
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val appModeRepository: AppModeRepository,
    private val profileStore: XrayProfileStore,
    private val updateXrayTransport: UpdateXrayTransportUseCase,
) : PatternViewModel<SettingsContract.State, SettingsContract.Intent, SettingsContract.Effect>(
    initialState(appModeRepository, profileStore)
) {

    init {
        appModeRepository.mode
            .onEach { mode -> reduce { copy(mode = mode) } }
            .launchIn(scope)
    }

    override suspend fun onIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.ModeChanged -> appModeRepository.setMode(intent.mode)

            is SettingsContract.Intent.RealityServerNameChanged -> updateProfile {
                val reality = security as? XraySecurity.Reality ?: return@updateProfile this
                copy(security = reality.copy(serverName = intent.value))
            }.also { reduce { copy(realityServerName = intent.value) } }

            is SettingsContract.Intent.FingerprintChanged -> updateProfile {
                val reality = security as? XraySecurity.Reality ?: return@updateProfile this
                copy(security = reality.copy(fingerprint = intent.value))
            }.also { reduce { copy(fingerprint = intent.value) } }

            is SettingsContract.Intent.TransportChanged -> {
                updateXrayTransport(intent.value)?.let { updated ->
                    reduce { copy(transport = updated.transport, muxEnabled = updated.muxEnabled) }
                }
            }

            is SettingsContract.Intent.MuxEnabledChanged -> updateProfile {
                copy(muxEnabled = intent.enabled)
            }.also { reduce { copy(muxEnabled = intent.enabled) } }

            is SettingsContract.Intent.MuxConcurrencyChanged -> updateProfile {
                copy(muxConcurrency = intent.value)
            }.also { reduce { copy(muxConcurrency = intent.value) } }

            SettingsContract.Intent.ExportVlessLinkRequested -> {
                profileStore.getActiveProfile()?.let { profile ->
                    sendEffect(SettingsContract.Effect.ShareVlessLink(profile.toVlessUri()))
                }
            }
        }
    }

    private inline fun updateProfile(mutate: XrayVpnProfile.() -> XrayVpnProfile) {
        profileStore.getActiveProfile()?.let { profile -> profileStore.saveActiveProfile(profile.mutate()) }
    }

    private companion object {
        fun initialState(
            appModeRepository: AppModeRepository,
            profileStore: XrayProfileStore,
        ): SettingsContract.State {
            val profile = profileStore.getActiveProfile()
            val reality = profile?.security as? XraySecurity.Reality
            return SettingsContract.State(
                mode = appModeRepository.mode.value,
                hasProfile = profile != null,
                realityServerName = reality?.serverName.orEmpty(),
                fingerprint = reality?.fingerprint ?: XrayFingerprint.EDGE,
                transport = profile?.transport ?: XrayTransport.Xhttp(),
                muxEnabled = profile?.muxEnabled ?: false,
                muxConcurrency = profile?.muxConcurrency ?: XrayVpnProfile.DEFAULT_MUX_CONCURRENCY,
            )
        }
    }
}
