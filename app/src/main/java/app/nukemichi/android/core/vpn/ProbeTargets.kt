package app.nukemichi.android.core.vpn

/**
 * Endpoints used to ask "is traffic still flowing through the tunnel".
 *
 * A pool rather than one fixed host, for two reasons. A single host makes every install produce
 * the same request to the same name on the same cadence, which is a pattern an observer gets for
 * free. And a host that is merely blocked - which brand-name endpoints routinely are on the
 * networks this app exists for - would otherwise read as a dead tunnel and drive an endless
 * reconnect loop, so the watchdog wants a second, different opinion before it believes the first.
 *
 * Every entry is a captive-portal check: it answers TLS on 443 and returns a response of a few
 * bytes, so a probe costs almost nothing and looks like what a phone does on any new network.
 */
internal object ProbeTargets {

    const val PORT = 443

    /** [url] is the endpoint's own connectivity-check path, not its front page. */
    data class ProbeTarget(val host: String, val url: String)

    private val TARGETS = listOf(
        ProbeTarget("cp.cloudflare.com", "https://cp.cloudflare.com/generate_204"),
        ProbeTarget("detectportal.firefox.com", "https://detectportal.firefox.com/success.txt"),
        ProbeTarget("www.msftconnecttest.com", "https://www.msftconnecttest.com/connecttest.txt"),
        ProbeTarget("captive.apple.com", "https://captive.apple.com/hotspot-detect.html"),
        ProbeTarget("connectivitycheck.gstatic.com", "https://connectivitycheck.gstatic.com/generate_204"),
    )

    /** Two distinct hosts, so a single blocked name cannot decide a probe round on its own. */
    fun secondOpinionHosts(): Pair<String, String> =
        TARGETS.shuffled().let { it[0].host to it[1].host }

    /** One endpoint, for config fields xray reads once and keeps for the session. */
    fun urlForWholeSession(): String = TARGETS.random().url
}
