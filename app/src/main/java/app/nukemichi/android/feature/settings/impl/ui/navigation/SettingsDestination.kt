package app.nukemichi.android.feature.settings.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.feature.dashboard.XrayLogsKey
import app.nukemichi.android.feature.hello.AdvancedModeIntroKey
import app.nukemichi.android.feature.hello.HelloKey
import app.nukemichi.android.feature.settings.SettingsKey
import app.nukemichi.android.feature.settings.impl.ui.screen.SettingsScreen
import app.nukemichi.android.platform.navigation.Destination
import app.nukemichi.android.platform.navigation.LocalAppNavigator
import javax.inject.Inject

class SettingsDestination @Inject constructor() : Destination<SettingsKey> {

    @Composable
    override fun Content(key: SettingsKey) {
        val navigator = LocalAppNavigator.current

        SettingsScreen(
            onNavigateToLogs = { navigator.navigate(XrayLogsKey) },
            onNavigateToAdvancedModeIntro = { navigator.navigate(AdvancedModeIntroKey) },
            onServerForgotten = { navigator.replaceAll(HelloKey) },
        )
    }
}
