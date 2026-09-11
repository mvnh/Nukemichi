package app.nukemichi.android.core.vpn

import kotlinx.coroutines.flow.Flow

interface XraySubscriptionStore {
    /** The stored subscriptions followed by every committed [update]. Decryption never runs on the collector's thread. */
    val subscriptions: Flow<List<XraySubscription>>

    /** Applies [transform] to the latest subscriptions under a lock, persists the result and returns it. */
    suspend fun update(transform: (List<XraySubscription>) -> List<XraySubscription>): List<XraySubscription>
}
