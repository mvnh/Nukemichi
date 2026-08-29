package app.nukemichi.android.feature.hello.impl.ui.navigation.di

import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.NavDestination
import app.nukemichi.android.feature.hello.AdvancedModeIntroKey
import app.nukemichi.android.feature.hello.HelloKey
import app.nukemichi.android.feature.hello.impl.ui.navigation.AdvancedModeIntroDestination
import app.nukemichi.android.feature.hello.impl.ui.navigation.HelloDestination
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class HelloNavigationModule {

    @Binds
    @IntoMap
    @NavDestination(HelloKey::class)
    abstract fun bindHelloDestination(
        destination: HelloDestination
    ): Destination<*>

    @Binds
    @IntoMap
    @NavDestination(AdvancedModeIntroKey::class)
    abstract fun bindAdvancedModeIntroDestination(
        destination: AdvancedModeIntroDestination
    ): Destination<*>
}
