package app.nukemichi.android.feature.wizard.impl.ui.mvi

import androidx.compose.runtime.Immutable
import app.nukemichi.android.platform.ui.util.UiText

@Immutable
internal sealed interface ConnectionCheckState {
    data object Idle : ConnectionCheckState

    /** First grace period after pressing Next, with no cancel affordance yet. */
    data object Checking : ConnectionCheckState

    /** Grace period elapsed and the SSH handshake is still pending, so cancel is now offered. */
    data object StillChecking : ConnectionCheckState

    data class Failed(val reason: UiText) : ConnectionCheckState

    /** First connection to this host: no cached or explicitly-entered fingerprint to check
     *  against, so [SshUntrustedHostException][app.nukemichi.android.core.ssh.model.SshUntrustedHostException]
     *  came back instead of a hard failure. The user gets to see and accept it (TOFU) rather
     *  than having to go find and type the fingerprint in themselves. */
    data class UntrustedHost(val fingerprint: String) : ConnectionCheckState

    /** The host presented a key that disagrees with the one already pinned for it. Separate from
     *  [UntrustedHost] because the two need opposite copy: this is what an intercepted connection
     *  looks like, and a rebuilt server is the only benign explanation. */
    data class HostKeyChanged(
        val fingerprint: String,
        val expectedFingerprint: String,
    ) : ConnectionCheckState
}

internal val ConnectionCheckState.isInProgress: Boolean
    get() = this is ConnectionCheckState.Checking || this is ConnectionCheckState.StillChecking
