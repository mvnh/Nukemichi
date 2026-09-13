package app.nukemichi.android.core.vpn

/**
 * Endpoints the watchdog probes to ask whether traffic still flows through the tunnel.
 *
 * A pool, not one fixed host: a single host would make every install hit the same name on the same
 * cadence, and a host that is merely blocked (routine on the networks this app exists for) would
 * read as a dead tunnel and drive an endless reconnect loop.
 *
 * Every entry is a captive-portal check, so a probe costs a few bytes and looks like what any
 * phone does on a new network.
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
