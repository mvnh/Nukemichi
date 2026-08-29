package app.nukemichi.android.feature.hello.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.feature.hello.AdvancedModeIntroKey
import app.nukemichi.android.feature.hello.impl.ui.screen.AdvancedModeIntroScreen
import javax.inject.Inject

class AdvancedModeIntroDestination @Inject constructor() : Destination<AdvancedModeIntroKey> {

    @Composable
    override fun Content(key: AdvancedModeIntroKey) {
        val navigator = LocalAppNavigator.current

        AdvancedModeIntroScreen(onBackClick = navigator::back)
    }
}
