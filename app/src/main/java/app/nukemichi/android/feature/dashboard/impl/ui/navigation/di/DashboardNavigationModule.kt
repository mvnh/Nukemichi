package app.nukemichi.android.feature.dashboard.impl.ui.navigation.di

import app.nukemichi.android.core.navigation.Destination
import app.nukemichi.android.core.navigation.NavDestination
import app.nukemichi.android.feature.dashboard.DashboardKey
import app.nukemichi.android.feature.dashboard.XrayLogsKey
import app.nukemichi.android.feature.dashboard.impl.ui.navigation.DashboardDestination
import app.nukemichi.android.feature.dashboard.impl.ui.navigation.XrayLogsDestination
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardNavigationModule {

    @Binds
    @IntoMap
    @NavDestination(DashboardKey::class)
    abstract fun bindDashboardDestination(
        destination: DashboardDestination
    ): Destination<*>

    @Binds
    @IntoMap
    @NavDestination(XrayLogsKey::class)
    abstract fun bindXrayLogsDestination(
        destination: XrayLogsDestination
    ): Destination<*>
}
