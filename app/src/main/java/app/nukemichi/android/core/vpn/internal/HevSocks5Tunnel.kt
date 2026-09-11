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

    fun start(tunInterface: ParcelFileDescriptor, socksEndpoint: SocksEndpoint) {
        check(!TProxyService.TProxyIsRunning()) { "hev SOCKS5 tunnel is already running." }
        // The native side only takes a path, so the SOCKS credentials have to touch the disk. They
        // are parsed during TProxyStartService and never read again, so the file is deleted the
        // moment that call returns rather than being left sitting in filesDir until the next boot.
        val configFile = writeConfig(socksEndpoint)
        try {
            check(TProxyService.TProxyStartService(configFile.absolutePath, tunInterface.fd)) {
                "Unable to start hev SOCKS5 tunnel."
            }
        } finally {
            configFile.delete()
        }
    }

    fun stop() {
        if (TProxyService.TProxyIsRunning()) {
            check(TProxyService.TProxyStopService()) { "Unable to stop hev SOCKS5 tunnel." }
        }
    }

    private fun writeConfig(socksEndpoint: SocksEndpoint): File =
        File(context.filesDir, CONFIG_RELATIVE_PATH).also { file ->
            file.parentFile?.mkdirs()
            file.writeText(HevTunnelConfigFactory.build(socksEndpoint))
        }

    private companion object {
        const val CONFIG_RELATIVE_PATH = "hev/socks5-tunnel.yaml"
    }
}
