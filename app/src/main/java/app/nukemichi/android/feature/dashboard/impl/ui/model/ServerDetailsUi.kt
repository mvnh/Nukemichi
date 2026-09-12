package app.nukemichi.android.feature.dashboard.impl.ui.model

import androidx.compose.runtime.Immutable
import app.nukemichi.android.core.vpn.spec.XrayFingerprint

@Immutable
internal data class ServerDetailsUi(
    val id: String,
    val name: String,
    val flag: String?,
    val stack: String,
    val address: String,
    val maskingAs: String?,
    val deployedAtMillis: Long,
    val fingerprint: XrayFingerprint?,
    val muxEnabled: Boolean,
    val muxConcurrency: Int,
)
