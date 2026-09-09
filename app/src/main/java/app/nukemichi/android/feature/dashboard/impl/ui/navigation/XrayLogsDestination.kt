package app.nukemichi.android.feature.dashboard.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.feature.dashboard.XrayLogsKey
import app.nukemichi.android.feature.dashboard.impl.ui.screen.XrayLogsScreen
import app.nukemichi.android.platform.navigation.Destination
import app.nukemichi.android.platform.navigation.LocalAppNavigator
import javax.inject.Inject

class XrayLogsDestination @Inject constructor() : Destination<XrayLogsKey> {

    @Composable
    override fun Content(key: XrayLogsKey) {
        val navigator = LocalAppNavigator.current

        XrayLogsScreen(onBackClick = navigator::back)
    }
}
