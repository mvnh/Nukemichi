package app.nukemichi.android.core.vpn

import java.util.UUID

fun newXrayId(): String = UUID.randomUUID().toString()

fun List<XraySubscription>.findServer(serverId: String): XrayVpnProfile? =
    firstNotNullOfOrNull { subscription -> subscription.servers.firstOrNull { it.id == serverId } }

fun List<XraySubscription>.findServerByAddress(serverAddress: String): XrayVpnProfile? =
    firstNotNullOfOrNull { subscription -> subscription.servers.firstOrNull { it.serverAddress == serverAddress } }

fun List<XraySubscription>.findSubscription(subscriptionId: String): XraySubscription? =
    firstOrNull { it.id == subscriptionId }

/**
 * Stores a freshly deployed [server]. A redeploy to an address that is already stored replaces that
 * entry in place, keeping its id, name and group, so running the wizard twice stays idempotent.
 * Otherwise the server joins [targetSubscriptionId], or [newSubscription] when that is null or has
 * been deleted in the meantime.
 */
fun List<XraySubscription>.withDeployedServer(
    server: XrayVpnProfile,
    targetSubscriptionId: String?,
    newSubscription: () -> XraySubscription,
): List<XraySubscription> {
    findServerByAddress(server.serverAddress)?.let { existing ->
        return withServerUpdated(existing.id) { server.copy(id = existing.id, name = existing.name) }
    }
    val target = targetSubscriptionId?.let(::findSubscription) ?: return this + newSubscription().copy(servers = listOf(server))
    return withSubscriptionUpdated(target.id) { it.copy(servers = it.servers + server) }
}

fun List<XraySubscription>.withServerUpdated(
    serverId: String,
    transform: (XrayVpnProfile) -> XrayVpnProfile,
): List<XraySubscription> = map { subscription ->
    if (subscription.servers.none { it.id == serverId }) {
        subscription
    } else {
        subscription.copy(servers = subscription.servers.map { if (it.id == serverId) transform(it) else it })
    }
}

/** A group left without servers is removed along with its last one. */
fun List<XraySubscription>.withoutServer(serverId: String): List<XraySubscription> = mapNotNull { subscription ->
    val remaining = subscription.servers.filterNot { it.id == serverId }
    when {
        remaining.size == subscription.servers.size -> subscription
        remaining.isEmpty() -> null
        else -> subscription.copy(servers = remaining)
    }
}

fun List<XraySubscription>.withoutSubscription(subscriptionId: String): List<XraySubscription> =
    filterNot { it.id == subscriptionId }

fun List<XraySubscription>.withSubscriptionUpdated(
    subscriptionId: String,
    transform: (XraySubscription) -> XraySubscription,
): List<XraySubscription> = map { if (it.id == subscriptionId) transform(it) else it }
