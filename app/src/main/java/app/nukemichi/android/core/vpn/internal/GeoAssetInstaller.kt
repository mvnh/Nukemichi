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
 * Stages geosite.dat and geoip.dat in filesDir, the only place `Libv2ray.initCoreEnv(...)` can
 * read them from: the APK's own assets/ is not a filesystem path the native core can open.
 *
 * A sidecar `.version` file records what is staged, so an already-current version short-circuits
 * instead of re-copying megabytes on every VPN start.
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
