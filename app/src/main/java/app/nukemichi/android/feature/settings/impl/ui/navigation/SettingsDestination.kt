package app.nukemichi.android.feature.settings.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.feature.settings.SettingsKey
import app.nukemichi.android.feature.settings.impl.ui.screen.SettingsScreen
import javax.inject.Inject

class SettingsDestination @Inject constructor() : Destination<SettingsKey> {

    @Composable
    override fun Content(key: SettingsKey) {
        SettingsScreen()
    }
}
