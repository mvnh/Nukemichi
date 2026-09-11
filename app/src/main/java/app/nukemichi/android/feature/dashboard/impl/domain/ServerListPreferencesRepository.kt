package app.nukemichi.android.feature.dashboard.impl.domain

import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import app.nukemichi.android.feature.dashboard.impl.domain.model.ServerListPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * How the user left the server list: which server connections use and which groups are folded away.
 * Neither is subscription data, so both stay with the dashboard in plain experience storage. The ids are
 * random UUIDs and say nothing about the servers behind them.
 */
@Singleton
internal class ServerListPreferencesRepository @Inject constructor(
    private val appStorage: AppStorage,
) {
    private val _preferences = MutableStateFlow(read())
    val preferences: StateFlow<ServerListPreferences> = _preferences.asStateFlow()

    fun select(serverId: String) = update { copy(selectedServerId = serverId) }

    fun setCollapsed(subscriptionId: String, collapsed: Boolean) = update {
        copy(
            collapsedSubscriptionIds = if (collapsed) {
                collapsedSubscriptionIds + subscriptionId
            } else {
                collapsedSubscriptionIds - subscriptionId
            },
        )
    }

    private fun update(transform: ServerListPreferences.() -> ServerListPreferences) {
        val next = _preferences.updateAndGet(transform)
        next.selectedServerId
            ?.let { appStorage.putString(StorageDomain.EXPERIENCE, ExperienceKeys.SELECTED_SERVER_ID, it) }
            ?: appStorage.remove(StorageDomain.EXPERIENCE, ExperienceKeys.SELECTED_SERVER_ID)
        appStorage.putString(
            StorageDomain.EXPERIENCE,
            ExperienceKeys.COLLAPSED_SUBSCRIPTION_IDS,
            next.collapsedSubscriptionIds.joinToString(SEPARATOR),
        )
    }

    private fun read() = ServerListPreferences(
        selectedServerId = appStorage.getString(StorageDomain.EXPERIENCE, ExperienceKeys.SELECTED_SERVER_ID),
        collapsedSubscriptionIds = appStorage.getString(StorageDomain.EXPERIENCE, ExperienceKeys.COLLAPSED_SUBSCRIPTION_IDS)
            ?.split(SEPARATOR)
            ?.filterTo(mutableSetOf(), String::isNotBlank)
            .orEmpty(),
    )

    private companion object {
        const val SEPARATOR = ","
    }
}
