package app.nukemichi.android.core.vpn.di

import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayProfileStore
import app.nukemichi.android.core.vpn.XrayServiceProvider
import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.internal.RemoteXrayMonitoring
import app.nukemichi.android.core.vpn.internal.StoredXrayProfileStore
import app.nukemichi.android.core.vpn.internal.XrayProcessControl
import app.nukemichi.android.core.vpn.internal.XrayRuntime
import app.nukemichi.android.core.vpn.internal.XrayServiceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class VpnModule {
    @Binds
    abstract fun bindXrayControl(impl: XrayProcessControl): XrayControl

    @Binds
    abstract fun bindXrayMonitoring(impl: RemoteXrayMonitoring): XrayMonitoring

    @Binds
    abstract fun bindXrayStatsSource(impl: XrayRuntime): XrayStatsSource

    @Binds
    abstract fun bindXrayProfileStore(impl: StoredXrayProfileStore): XrayProfileStore

    @Binds
    abstract fun bindXrayServiceProvider(impl: XrayServiceProviderImpl): XrayServiceProvider
}
