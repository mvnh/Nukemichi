package app.nukemichi.android.core.vpn

interface XrayProfileStore {
    fun getActiveProfile(): XrayVpnProfile?
    fun saveActiveProfile(profile: XrayVpnProfile)
    fun clearActiveProfile()
}
