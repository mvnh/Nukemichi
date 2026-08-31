package app.nukemichi.android.core.ui.util

import androidx.compose.runtime.Stable

@Stable
@JvmInline
value class UiSecret(val value: String) {
    override fun toString(): String = "UiSecret(***)"

    companion object {
        val Empty = UiSecret("")
    }
}
