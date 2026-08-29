package app.nukemichi.android.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import dagger.MapKey
import kotlin.reflect.KClass

interface Destination<K : NavKey> {

    @Composable
    fun Content(key: K)
}

@MapKey
@Target(AnnotationTarget.FUNCTION)
annotation class NavDestination(
    val value: KClass<out NavKey>
)
