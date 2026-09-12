package app.nukemichi.android.core.vpn.internal

import android.content.Context
import android.os.ParcelFileDescriptor
import app.nukemichi.android.core.vpn.SocksEndpoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HevSocks5Tunnel @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    // The native API only takes a path, so the SOCKS credentials have to be written somewhere.
    // They are deleted in stop() rather than straight after start(): hev-jni's
    // native_start_service returns as soon as it has pthread_create'd its worker, and the config
    // is read on that worker afterwards, so deleting on the way out of start() is a race.
    // native_stop_service joins that thread, which makes stop() the first point where nothing can
    // still be reading the file.
    fun start(tunInterface: ParcelFileDescriptor, socksEndpoint: SocksEndpoint) {
        check(!TProxyService.TProxyIsRunning()) { "hev SOCKS5 tunnel is already running." }
        val configFile = writeConfig(socksEndpoint)
        check(TProxyService.TProxyStartService(configFile.absolutePath, tunInterface.fd)) {
            "Unable to start hev SOCKS5 tunnel."
        }
    }

    fun stop() {
        try {
            if (TProxyService.TProxyIsRunning()) {
                check(TProxyService.TProxyStopService()) { "Unable to stop hev SOCKS5 tunnel." }
            }
        } finally {
            // Unconditional: it also sweeps up a config left behind by a start that failed, or by
            // a build from before this was cleaned up at all.
            configFile().delete()
        }
    }

    private fun configFile(): File = File(context.filesDir, CONFIG_RELATIVE_PATH)

    private fun writeConfig(socksEndpoint: SocksEndpoint): File =
        configFile().also { file ->
            file.parentFile?.mkdirs()
            file.writeText(HevTunnelConfigFactory.build(socksEndpoint))
        }

    private companion object {
        const val CONFIG_RELATIVE_PATH = "hev/socks5-tunnel.yaml"
    }
}
