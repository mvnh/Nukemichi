package app.nukemichi.android.core.vpn

interface XrayStatsSource {
    fun queryAllOutboundTrafficStats(): String?
}
