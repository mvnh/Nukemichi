package app.nukemichi.android.feature.settings.impl.domain.usecase

import app.nukemichi.android.core.vpn.XrayProfileStore
import app.nukemichi.android.core.vpn.XrayVpnProfile
import app.nukemichi.android.core.vpn.spec.XrayFlow
import app.nukemichi.android.core.vpn.spec.XrayTransport
import javax.inject.Inject

internal class UpdateXrayTransportUseCase @Inject constructor(
    private val profileStore: XrayProfileStore,
) {
    operator fun invoke(transport: XrayTransport): XrayVpnProfile? {
        val profile = profileStore.getActiveProfile() ?: return null
        val carriesVision = transport is XrayTransport.Raw && transport.flow == XrayFlow.VISION
        val updated = profile.copy(
            transport = transport,
            muxEnabled = if (carriesVision) false else profile.muxEnabled,
        )
        profileStore.saveActiveProfile(updated)
        return updated
    }
}
