package app.nukemichi.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.nukemichi.android.core.navigation.AppNavigator
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.core.ui.theme.NukemichiTheme
import app.nukemichi.android.feature.dashboard.DashboardKey
import app.nukemichi.android.feature.hello.HelloKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var destinations: Map<Class<out NavKey>, @JvmSuppressWildcards Destination<*>>

    @Inject
    lateinit var appStorage: AppStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hasCompletedWizard = appStorage.getString(StorageDomain.EXPERIENCE, ExperienceKeys.WIZARD_COMPLETED) != null
        val startKey: NavKey = if (hasCompletedWizard) DashboardKey else HelloKey

        setContent {
            NukemichiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val backStack = rememberNavBackStack(startKey)

                    val navigator = remember(backStack) {
                        object : AppNavigator {
                            override fun navigate(key: NavKey) {
                                backStack.add(key)
                            }

                            override fun back() {
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            }

                            override fun replaceAll(key: NavKey) {
                                backStack.clear()
                                backStack.add(key)
                            }
                        }
                    }

                    CompositionLocalProvider(LocalAppNavigator provides navigator) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = navigator::back,
                            transitionSpec = {
                                slideInHorizontally(initialOffsetX = { it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it })
                            },
                            popTransitionSpec = {
                                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it })
                            },
                            predictivePopTransitionSpec = {
                                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it })
                            },
                        ) { key ->
                            val rawDestination = requireNotNull(destinations[key::class.java]) {
                                "Destination not found for key: ${key::class.qualifiedName}. Check your Hilt @NavDestination binding."
                            }

                            @Suppress("UNCHECKED_CAST")
                            val destination = rawDestination as Destination<NavKey>

                            NavEntry(key) {
                                destination.Content(key = key)
                            }
                        }
                    }
                }
            }
        }
    }
}