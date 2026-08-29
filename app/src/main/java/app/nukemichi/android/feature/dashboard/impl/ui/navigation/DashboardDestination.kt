package app.nukemichi.android.feature.dashboard.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.feature.dashboard.DashboardKey
import app.nukemichi.android.feature.dashboard.XrayLogsKey
import app.nukemichi.android.feature.dashboard.impl.ui.screen.DashboardScreen
import javax.inject.Inject

class DashboardDestination @Inject constructor() : Destination<DashboardKey> {

    @Composable
    override fun Content(key: DashboardKey) {
        val navigator = LocalAppNavigator.current

        DashboardScreen(onNavigateToLogs = { navigator.navigate(XrayLogsKey) })
    }
}
