package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.vpn.XrayControl
import app.nukemichi.android.core.vpn.XrayMonitoring
import app.nukemichi.android.core.vpn.XrayServiceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class XrayServiceProviderImpl @Inject constructor(
    override val control: XrayControl,
    override val monitoring: XrayMonitoring,
) : XrayServiceProvider
