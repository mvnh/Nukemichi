package app.nukemichi.android.core.vpn.internal

import android.content.Context
import android.net.VpnService
import androidx.core.content.ContextCompat
import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayRuntimeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class XrayProcessControl @Inject constructor(
    @ApplicationContext private val context: Context,
) : XrayControl {

    override fun needsVpnPermission(): Boolean = VpnService.prepare(context) != null

    override suspend fun start(config: XrayRuntimeConfig): Result<Unit> = runCatching {
        require(!needsVpnPermission()) { "VPN permission must be granted before starting Xray." }
        ContextCompat.startForegroundService(
            context,
            NukemichiVpnService.startIntent(context, config)
        )
    }

    override suspend fun reload(config: XrayRuntimeConfig): Result<Unit> = runCatching {
        require(!needsVpnPermission()) { "VPN permission must be granted before reloading Xray." }
        ContextCompat.startForegroundService(
            context,
            NukemichiVpnService.reloadIntent(context, config)
        )
    }

    override suspend fun stop(): Result<Unit> = runCatching {
        context.startService(NukemichiVpnService.stopIntent(context))
    }
}
