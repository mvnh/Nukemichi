package app.nukemichi.android.core.vpn

// Neither a server nor a group should show up as a bare IP or UUID. Deterministic per [seed]: a server
// seeded by its address keeps its name across redeploys (the wizard's idempotent-by-design contract),
// while a group seeded by its random id gets a random name.
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
