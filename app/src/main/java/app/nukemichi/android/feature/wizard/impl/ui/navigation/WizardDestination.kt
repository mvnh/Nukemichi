package app.nukemichi.android.feature.wizard.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.feature.dashboard.DashboardKey
import app.nukemichi.android.feature.wizard.WizardKey
import app.nukemichi.android.feature.wizard.impl.ui.screen.WizardScreen
import javax.inject.Inject

class WizardDestination @Inject constructor() : Destination<WizardKey> {

    @Composable
    override fun Content(key: WizardKey) {
        val navigator = LocalAppNavigator.current

        WizardScreen(
            onNavigateBack = navigator::back,
            onNavigateToDashboard = { navigator.replaceAll(DashboardKey) }
        )
    }
}
