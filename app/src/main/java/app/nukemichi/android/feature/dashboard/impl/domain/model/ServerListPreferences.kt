package app.nukemichi.android.feature.dashboard.impl.domain.model

internal data class ServerListPreferences(
    val selectedServerId: String? = null,
    val collapsedSubscriptionIds: Set<String> = emptySet(),
)
