package app.nukemichi.android.core.vpn.di

import android.os.SystemClock
import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayServiceProvider
import app.nukemichi.android.core.vpn.XrayStatsSource
import app.nukemichi.android.core.vpn.XraySubscriptionStore
import app.nukemichi.android.core.vpn.internal.ElapsedRealtimeSource
import app.nukemichi.android.core.vpn.internal.RemoteXrayMonitoring
import app.nukemichi.android.core.vpn.internal.StoredXraySubscriptionStore
import app.nukemichi.android.core.vpn.internal.XrayProcessControl
import app.nukemichi.android.core.vpn.internal.XrayRuntime
import app.nukemichi.android.core.vpn.internal.XrayServiceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
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
    abstract fun bindXraySubscriptionStore(impl: StoredXraySubscriptionStore): XraySubscriptionStore

    @Binds
    abstract fun bindXrayServiceProvider(impl: XrayServiceProviderImpl): XrayServiceProvider

    companion object {
        // @Provides, not @Binds: the implementation is one platform call and needs no class.
        @Provides
        fun provideElapsedRealtimeSource(): ElapsedRealtimeSource =
            ElapsedRealtimeSource(SystemClock::elapsedRealtime)
    }
}
