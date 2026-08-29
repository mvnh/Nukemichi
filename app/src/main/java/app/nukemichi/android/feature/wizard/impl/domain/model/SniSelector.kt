package app.nukemichi.android.feature.wizard.impl.domain.model

internal object SniSelector {
    private val blockedTlds = setOf(
        ".ru",
        ".by",
        // Dynamic-DNS providers point at a single, usually home-hosted machine rather than a
        // server built for sustained HTTP/2 traffic — RealiTLScanner only checks that the TLS
        // handshake itself succeeds, not that the backend survives real XHTTP load. Confirmed in
        // practice: a duckdns.org pick accepted the handshake but dropped every XHTTP POST
        // mid-request (EOF), making every proxied request unreliable despite the SNI "working".
        ".duckdns.org",
        ".no-ip.org",
        ".no-ip.com",
        ".no-ip.info",
        ".ddns.net",
        ".dynu.com",
        ".dyndns.org",
    )

    private val blockedDomains = setOf(
        "vk.com",
        "yahoo.com",
        "microsoft.com",
        "google.com",
        "googlevideo.com",
        "googleapis.com",
        "apple.com",
        "cloudflare.com",
        "github.com",
        "fastly.net",
    )

    // Certificate SANs are frequently wildcards (e.g. "*.userapi.com"); scraping RealiTLScanner's
    // output can surface those verbatim. A wildcard isn't a valid literal SNI/Host value — using
    // one breaks the REALITY handshake and XHTTP dial silently (TCP connects, TLS/HTTP never
    // completes), so candidates must be a plain, valid hostname.
    private val validHostname = Regex("""^(?!-)[a-z0-9-]{1,63}(?<!-)(\.(?!-)[a-z0-9-]{1,63}(?<!-))+$""")

    fun isAllowed(domain: String): Boolean {
        val normalized = domain.trim().lowercase().removeSuffix(".")
        if (normalized.isEmpty()) return false
        if (!validHostname.matches(normalized)) return false
        if (blockedTlds.any { normalized.endsWith(it) }) return false
        if (blockedDomains.any { normalized == it || normalized.endsWith(".$it") }) return false
        return true
    }
}
