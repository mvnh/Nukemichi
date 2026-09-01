package app.nukemichi.android.feature.wizard.impl.domain.model

internal object SniSelector {
    // A masking domain hosted in the same jurisdiction as the censorship being avoided defeats the
    // point: the traffic reads as ordinary domestic traffic, which is exactly what gets inspected.
    private val blockedTlds = setOf(
        ".ru",
        ".by",
    )

    // Dynamic-DNS names usually point at a single home-hosted machine, not at a server built for
    // sustained HTTP/2 traffic. RealiTLScanner only proves the TLS handshake succeeds, so such a
    // host can pass the scan and still drop XHTTP POSTs mid-request.
    private val blockedDynamicDnsSuffixes = setOf(
        ".duckdns.org",
        ".no-ip.org",
        ".no-ip.com",
        ".no-ip.info",
        ".ddns.net",
        ".dynu.com",
        ".dyndns.org",
    )

    // Names fronted by a global CDN or anycast edge. Their TLS terminates wherever the client is,
    // not near the VPS, so a REALITY handshake claiming one from a single VPS address is
    // geographically implausible in a way an observer can check cheaply.
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
        if (blockedDynamicDnsSuffixes.any { normalized.endsWith(it) }) return false
        if (blockedDomains.any { normalized == it || normalized.endsWith(".$it") }) return false
        return true
    }
}
