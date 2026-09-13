package app.nukemichi.android.core.ssh.internal

/**
 * What trust-on-first-use knows about a host, as three states rather than a nullable fingerprint.
 * [Unreadable] is the one a nullable would swallow: folding it into "no pin" turns a host the user
 * once verified back into a routine first connection, the prompt they are likeliest to accept
 * without reading.
 */
internal sealed interface HostKeyPin {
    data object None : HostKeyPin
    data class Known(val fingerprint: String) : HostKeyPin
    data object Unreadable : HostKeyPin

    fun matches(candidate: String): Boolean = this is Known && fingerprint == candidate
}
