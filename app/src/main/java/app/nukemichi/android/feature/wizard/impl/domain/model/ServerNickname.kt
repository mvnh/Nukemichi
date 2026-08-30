package app.nukemichi.android.feature.wizard.impl.domain.model

// The dashboard shouldn't have to show a bare IP as "the server" — its own address is already
// visible lower on that screen. Deterministic per-[seed] so redeploying to the same server (the
// wizard's own idempotent-by-design contract) keeps the same name instead of reshuffling it.
internal fun generateServerNickname(seed: String): String {
    val hash = seed.hashCode()
    val adjective = ADJECTIVES[Math.floorMod(hash, ADJECTIVES.size)]
    val noun = NOUNS[Math.floorMod(hash / ADJECTIVES.size, NOUNS.size)]
    return "$adjective $noun"
}

private val ADJECTIVES = listOf(
    "Quiet", "Hidden", "Midnight", "Silent", "Shadow", "Steady", "Swift", "Loyal",
)
private val NOUNS = listOf(
    "Harbor", "Falcon", "Outpost", "Beacon", "Sentinel", "Waypoint", "Relay", "Haven",
)
