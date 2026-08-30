package app.nukemichi.android.core.ui.util

import androidx.compose.runtime.Stable

/**
 * A user-entered secret while it is still in the UI layer — the presentation-side counterpart to
 * `core.security.Secret`, kept separate because UI state must not hold data-layer models. Mapping
 * between the two belongs at the layer boundary (see the wizard's state mappers), not in the state
 * or the intents themselves.
 *
 * The masked [toString] is the whole point: MVI state and intents are `data class`es, so they build
 * their own `toString` from their properties and print in full the moment anyone logs one. Wrapping
 * the value makes that safe by type rather than by remembering, which is the same reasoning as
 * `ShellSafe` in `core.ssh`.
 */
@Stable
@JvmInline
value class UiSecret(val value: String) {
    override fun toString(): String = "UiSecret(***)"

    companion object {
        val Empty = UiSecret("")
    }
}
