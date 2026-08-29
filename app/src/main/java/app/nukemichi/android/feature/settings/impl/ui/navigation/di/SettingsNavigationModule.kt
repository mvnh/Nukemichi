package app.nukemichi.android.feature.settings.impl.ui.navigation.di

import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.NavDestination
import app.nukemichi.android.feature.settings.SettingsKey
import app.nukemichi.android.feature.settings.impl.ui.navigation.SettingsDestination
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsNavigationModule {

    @Binds
    @IntoMap
    @NavDestination(SettingsKey::class)
    abstract fun bindSettingsDestination(
        destination: SettingsDestination
    ): Destination<*>
}
