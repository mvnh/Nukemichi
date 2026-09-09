package app.nukemichi.android.feature.wizard.impl.ui.navigation.di

import app.nukemichi.android.feature.wizard.WizardKey
import app.nukemichi.android.feature.wizard.impl.ui.navigation.WizardDestination
import app.nukemichi.android.platform.navigation.Destination
import app.nukemichi.android.platform.navigation.NavDestination
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class WizardNavigationModule {

    @Binds
    @IntoMap
    @NavDestination(WizardKey::class)
    abstract fun bindWizardDestination(
        destination: WizardDestination
    ): Destination<*>
}
