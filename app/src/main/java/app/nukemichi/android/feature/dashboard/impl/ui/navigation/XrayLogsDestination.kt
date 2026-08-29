package app.nukemichi.android.feature.dashboard.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.feature.dashboard.XrayLogsKey
import app.nukemichi.android.feature.dashboard.impl.ui.screen.XrayLogsScreen
import javax.inject.Inject

class XrayLogsDestination @Inject constructor() : Destination<XrayLogsKey> {

    @Composable
    override fun Content(key: XrayLogsKey) {
        val navigator = LocalAppNavigator.current

        XrayLogsScreen(onBackClick = navigator::back)
    }
}
