package app.nukemichi.android.core.vpn.internal

import android.content.Context
import app.nukemichi.android.BuildConfig
import app.nukemichi.android.platform.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Stages geosite.dat and geoip.dat in the app's filesDir - the only place Xray's
 * `Libv2ray.initCoreEnv(...)` asset lookup can read them from (see [XrayRuntime]). The APK's own
 * assets/ directory isn't a real filesystem path the native core can open, so this is a one-time
 * copy rather than something the core can be pointed at directly.
 *
 * Each file is copied once per pinned version: a sidecar `.version` file records what's staged,
 * so a version that's already current short-circuits to a no-op instead of re-copying a few
 * hundred KB to a few MB on every VPN start.
 */
@Singleton
internal class GeoAssetInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun ensureInstalled() = withContext(ioDispatcher) {
        install(GEOSITE_ASSET, BuildConfig.GEOSITE_DAT_VERSION)
        install(GEOIP_ASSET, BuildConfig.GEOIP_DAT_SHA256)
    }

    private fun install(assetName: String, version: String) {
        val target = File(context.filesDir, assetName)
        val marker = File(context.filesDir, "$assetName.version")
        if (target.exists() && marker.readVersionOrNull() == version) return

        context.assets.open(assetName).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        marker.writeText(version)
    }

    private fun File.readVersionOrNull(): String? = if (exists()) readText() else null

    private companion object {
        const val GEOSITE_ASSET = "geosite.dat"
        const val GEOIP_ASSET = "geoip.dat"
    }
}
