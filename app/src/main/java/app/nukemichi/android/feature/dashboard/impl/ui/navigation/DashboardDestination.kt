package app.nukemichi.android.feature.dashboard.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.feature.dashboard.DashboardKey
import app.nukemichi.android.feature.dashboard.impl.ui.screen.DashboardScreen
import app.nukemichi.android.feature.settings.SettingsKey
import app.nukemichi.android.feature.wizard.WizardKey
import app.nukemichi.android.platform.navigation.Destination
import app.nukemichi.android.platform.navigation.LocalAppNavigator
import javax.inject.Inject

class DashboardDestination @Inject constructor() : Destination<DashboardKey> {

    @Composable
    override fun Content(key: DashboardKey) {
        val navigator = LocalAppNavigator.current

        DashboardScreen(
            selectServerId = key.selectServerId,
            onSettingsClick = { navigator.navigate(SettingsKey) },
            onNavigateToWizard = { subscriptionId -> navigator.navigate(WizardKey(subscriptionId)) },
        )
    }
}
