package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.SecureStorageUnreadableException
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.core.vpn.XrayJson
import app.nukemichi.android.core.vpn.XrayProfileStore
import app.nukemichi.android.core.vpn.XrayVpnProfile
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class StoredXrayProfileStore @Inject constructor(
    private val appStorage: AppStorage,
) : XrayProfileStore {
    override fun getActiveProfile(): XrayVpnProfile? {
        // The stored profile is left in place when it cannot be decrypted: the dashboard degrades
        // to "no server configured", which is recoverable, whereas deleting it is not.
        val payload = try {
            appStorage.getString(StorageDomain.XRAY_PROFILES, KEY_ACTIVE_PROFILE) ?: return null
        } catch (error: SecureStorageUnreadableException) {
            Timber.e(error, "Active profile is unreadable")
            return null
        }
        return runCatching { XrayJson.default.decodeFromString<XrayVpnProfile>(payload) }
            .getOrElse {
                appStorage.remove(StorageDomain.XRAY_PROFILES, KEY_ACTIVE_PROFILE)
                null
            }
    }

    override fun saveActiveProfile(profile: XrayVpnProfile) {
        appStorage.putString(
            StorageDomain.XRAY_PROFILES,
            KEY_ACTIVE_PROFILE,
            XrayJson.default.encodeToString(profile)
        )
    }

    override fun clearActiveProfile() {
        appStorage.remove(StorageDomain.XRAY_PROFILES, KEY_ACTIVE_PROFILE)
    }

    private companion object {
        const val KEY_ACTIVE_PROFILE = "active-profile"
    }
}
