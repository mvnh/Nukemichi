package app.nukemichi.android.core.ssh.internal.model

import app.nukemichi.android.core.ssh.SshConnection
import kotlinx.coroutines.Job

internal class SharedConnection(
    val connection: SshConnection,
) {
    var leases: Int = 0
        private set

    var idleCloseJob: Job? = null

    fun lease() {
        leases += 1
    }

    fun unlease(): Int {
        leases -= 1
        check(leases >= 0) { "SSH connection lease underflow." }
        return leases
    }
}
