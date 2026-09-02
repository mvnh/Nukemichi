package app.nukemichi.android.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource

@Stable
sealed interface UiText {

    data class Raw(val value: String) : UiText

    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }

    data object Empty : UiText
}

@Composable
@ReadOnlyComposable
fun UiText.asString(): String {
    return when (this) {
        is UiText.Raw -> value
        is UiText.Resource -> stringResource(resId, *args.toTypedArray())
        is UiText.Empty -> ""
    }
}

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.Raw -> value
        is UiText.Resource -> context.getString(resId, *args.toTypedArray())
        is UiText.Empty -> ""
    }
}
