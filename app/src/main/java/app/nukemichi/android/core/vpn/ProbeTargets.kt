package app.nukemichi.android.core.vpn

/**
 * Hosts used to ask "is traffic still flowing through the tunnel".
 *
 * A pool rather than one fixed host, for two reasons. A single host makes every install produce
 * the same request to the same name on the same cadence, which is a pattern an observer gets for
 * free. And a host that is merely blocked - which brand-name endpoints routinely are on the
 * networks this app exists for - would otherwise read as a dead tunnel and drive an endless
 * reconnect loop, so the watchdog wants a second, different opinion before it believes the first.
 *
 * Every entry answers TLS on 443 and a bare HTTP request with a small, fixed response.
 */
internal object ProbeTargets {

    const val PORT = 443

    private val HOSTS = listOf(
        "cp.cloudflare.com",
        "detectportal.firefox.com",
        "www.msftconnecttest.com",
        "captive.apple.com",
        "connectivitycheck.gstatic.com",
    )

    /** Two distinct hosts, so a single blocked name cannot decide a round on its own. */
    fun pair(): Pair<String, String> {
        val shuffled = HOSTS.shuffled()
        return shuffled[0] to shuffled[1]
    }

    /** A single host, for config fields that are fixed for the lifetime of a session. */
    fun random(): String = HOSTS.random()
}
