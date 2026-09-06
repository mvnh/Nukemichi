package app.nukemichi.android.feature.settings.impl.ui.mvi

import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.core.vpn.spec.XrayTransport
import app.nukemichi.android.platform.mode.AppMode

internal object SettingsContract {

    data class State(
        val mode: AppMode = AppMode.NORMAL,
        val hasProfile: Boolean = false,
        val fingerprint: XrayFingerprint = XrayFingerprint.EDGE,
        val transport: XrayTransport = XrayTransport.Xhttp(),
        val muxEnabled: Boolean = false,
        val muxConcurrency: Int = 0,
    )

    sealed interface Intent {
        data class AdvancedModeToggled(val enabled: Boolean) : Intent
        data object ViewLogsRequested : Intent
        data object ForgetServerRequested : Intent
        data class FingerprintChanged(val value: XrayFingerprint) : Intent
        data class TransportChanged(val value: XrayTransport) : Intent
        data class MuxEnabledChanged(val enabled: Boolean) : Intent
        data class MuxConcurrencyChanged(val value: Int) : Intent
        data object ExportVlessLinkRequested : Intent
    }

    sealed interface Effect {
        data class ShareVlessLink(val uri: String) : Effect
        data object NavigateToAdvancedModeIntro : Effect
        data object NavigateToLogs : Effect
        data object ServerForgotten : Effect
    }
}

internal val SettingsContract.State.isAdvanced: Boolean
    get() = mode == AppMode.ADVANCED
