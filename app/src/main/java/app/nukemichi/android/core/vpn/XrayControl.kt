package app.nukemichi.android.core.vpn

interface XrayControl {
    fun needsVpnPermission(): Boolean
    suspend fun start(config: XrayRuntimeConfig): Result<Unit>
    suspend fun reload(config: XrayRuntimeConfig): Result<Unit>
    suspend fun stop(): Result<Unit>
}
