package app.nukemichi.android.core.vpn

import kotlinx.coroutines.flow.Flow

interface XraySubscriptionStore {
    /** Decryption never runs on the collector's thread. */
    val subscriptions: Flow<List<XraySubscription>>

    /** Applies [transform] under a lock, so concurrent updates never interleave. */
    suspend fun update(transform: (List<XraySubscription>) -> List<XraySubscription>): List<XraySubscription>
}
