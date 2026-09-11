package app.nukemichi.android.core.vpn

// Deterministic per [seed], so redeploying to the same address keeps the same name.
fun generateNickname(seed: String): String {
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
