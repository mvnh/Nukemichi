package app.nukemichi.android.core.ssh.internal.model

import app.nukemichi.android.core.ssh.LpfHandle
import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.model.CommandEvent
import app.nukemichi.android.core.ssh.model.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * The lease count is what SshjManager uses to decide a connection is idle and can be closed. An
 * over-count leaks a live SSH session for the process's lifetime; an under-count closes a
 * connection while another caller is still running commands over it — which surfaces as a
 * mid-deployment failure with no obvious cause.
 */
class SharedConnectionTest {

    @Test
    fun `unlease reports the remaining lease count`() {
        val shared = SharedConnection(NoopSshConnection())

        shared.lease()
        shared.lease()

        assertEquals("one holder left, not idle yet", 1, shared.unlease())
        assertEquals("last holder released, now idle", 0, shared.unlease())
    }

    @Test
    fun `a fresh connection starts unleased`() {
        assertEquals(0, SharedConnection(NoopSshConnection()).leases)
    }

    /**
     * Releasing more often than acquiring would drive the count negative, and a negative count
     * never reaches the zero that triggers the idle close — the session would be pinned open
     * forever instead. Better to fail loudly at the bug than to leak quietly.
     */
    @Test
    fun `releasing more than was acquired fails loudly`() {
        val shared = SharedConnection(NoopSshConnection())
        shared.lease()
        shared.unlease()

        assertThrows(IllegalStateException::class.java) { shared.unlease() }
    }
}

private class NoopSshConnection : SshConnection {
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(true)
    override fun refreshConnectionState(): Boolean = true
    override fun executeStreaming(command: String, args: List<String>): Flow<CommandEvent> = emptyFlow()
    override suspend fun execute(command: String, args: List<String>): Result<CommandResult> =
        Result.success(CommandResult("", "", 0))

    override suspend fun upload(remotePath: String, content: ByteArray, permissions: Int?): Result<Unit> =
        Result.success(Unit)

    override suspend fun startLpf(remotePort: Int): Result<LpfHandle> =
        throw UnsupportedOperationException()

    override suspend fun disconnect() = Unit
}
