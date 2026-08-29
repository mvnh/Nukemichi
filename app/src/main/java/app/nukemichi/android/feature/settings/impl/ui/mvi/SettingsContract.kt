package app.nukemichi.android.feature.settings.impl.ui.mvi

import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.vpn.spec.XrayFingerprint
import app.nukemichi.android.core.vpn.spec.XrayTransport

internal object SettingsContract {

    data class State(
        val mode: AppMode = AppMode.NORMAL,
        val hasProfile: Boolean = false,
        val realityServerName: String = "",
        val fingerprint: XrayFingerprint = XrayFingerprint.EDGE,
        val transport: XrayTransport = XrayTransport.Xhttp(),
        val muxEnabled: Boolean = false,
        val muxConcurrency: Int = 0,
    )

    sealed interface Intent {
        data class ModeChanged(val mode: AppMode) : Intent
        data class RealityServerNameChanged(val value: String) : Intent
        data class FingerprintChanged(val value: XrayFingerprint) : Intent
        data class TransportChanged(val value: XrayTransport) : Intent
        data class MuxEnabledChanged(val enabled: Boolean) : Intent
        data class MuxConcurrencyChanged(val value: Int) : Intent
        data object ExportVlessLinkRequested : Intent
    }

    sealed interface Effect {
        data class ShareVlessLink(val uri: String) : Effect
    }
}

internal val SettingsContract.State.isAdvanced: Boolean
    get() = mode == AppMode.ADVANCED
