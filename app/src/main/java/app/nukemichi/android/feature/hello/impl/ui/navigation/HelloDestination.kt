package app.nukemichi.android.feature.hello.impl.ui.navigation

import androidx.compose.runtime.Composable
import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.LocalAppNavigator
import app.nukemichi.android.feature.hello.HelloKey
import app.nukemichi.android.feature.hello.impl.ui.screen.HelloScreen
import app.nukemichi.android.feature.wizard.WizardFlow
import app.nukemichi.android.feature.wizard.WizardKey
import javax.inject.Inject

class HelloDestination @Inject constructor() : Destination<HelloKey> {

    @Composable
    override fun Content(key: HelloKey) {
        val navigator = LocalAppNavigator.current

        HelloScreen(
            onSetUpServerClick = { navigator.navigate(WizardKey(WizardFlow.DEPLOY_SERVER)) },
        )
    }
}
