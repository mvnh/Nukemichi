package app.nukemichi.android.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

interface AppNavigator {
    fun navigate(key: NavKey)
    fun back()
    fun replaceAll(key: NavKey)
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("AppNavigator is not provided")
}
