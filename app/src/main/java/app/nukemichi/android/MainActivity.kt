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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.nukemichi.android.core.di.IoDispatcher
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var destinations: Map<Class<out NavKey>, @JvmSuppressWildcards Destination<*>>

    @Inject
    lateinit var appStorage: AppStorage

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NukemichiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Which screen opens depends on a stored flag, and reading it touches disk.
                    // The Surface above is already painted, so resolving it off the main thread
                    // costs a themed frame rather than a stalled one.
                    val startKey by produceState<NavKey?>(initialValue = null) {
                        value = withContext(ioDispatcher) {
                            val done = appStorage.getBoolean(
                                StorageDomain.EXPERIENCE,
                                ExperienceKeys.WIZARD_COMPLETED,
                            )
                            if (done) DashboardKey else HelloKey
                        }
                    }
                    val resolvedStartKey = startKey ?: return@Surface

                    val backStack = rememberNavBackStack(resolvedStartKey)

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
                                "No Destination registered for ${key::class.qualifiedName}"
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