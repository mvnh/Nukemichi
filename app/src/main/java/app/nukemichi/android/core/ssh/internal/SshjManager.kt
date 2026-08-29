package app.nukemichi.android.core.ssh.internal

import app.nukemichi.android.core.ssh.SshConnection
import app.nukemichi.android.core.ssh.SshManager
import app.nukemichi.android.core.ssh.internal.model.SessionKey
import app.nukemichi.android.core.ssh.internal.model.SharedConnection
import app.nukemichi.android.core.ssh.internal.util.SecurityUtils
import app.nukemichi.android.core.ssh.model.SshAuth
import app.nukemichi.android.core.ssh.model.SshConfig
import app.nukemichi.android.core.ssh.model.SshUntrustedHostException
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.StorageDomain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import timber.log.Timber
import java.security.PublicKey
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

internal class SshjManager(
    private val ioDispatcher: CoroutineDispatcher,
    private val appStorage: AppStorage,
) : SshManager {

    private val sessionsMutex = Mutex()
    private val sessions = mutableMapOf<SessionKey, SharedConnection>()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override suspend fun <T> withConnection(
        config: SshConfig,
        auth: SshAuth,
        block: suspend (SshConnection) -> Result<T>,
    ): Result<T> {
        val key = SessionKey.of(config, auth)
        val shared = acquire(key, config, auth).getOrElse { return Result.failure(it) }

        return try {
            block(shared.connection)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Result.failure(error)
        } finally {
            release(key, shared)
        }
    }

    private suspend fun acquire(
        key: SessionKey,
        config: SshConfig,
        auth: SshAuth,
    ): Result<SharedConnection> = sessionsMutex.withLock {
        sessions[key]?.let { shared ->
            if (shared.connection.isConnected.value) {
                shared.idleCloseJob?.cancel()
                shared.idleCloseJob = null
                shared.lease()
                return@withLock Result.success(shared)
            }
            sessions.remove(key)
            shared.idleCloseJob?.cancel()
        }

        createConnection(config, auth).map { connection ->
            SharedConnection(connection).also {
                it.lease()
                sessions[key] = it
            }
        }
    }

    private suspend fun release(key: SessionKey, shared: SharedConnection) {
        sessionsMutex.withLock {
            if (shared.unlease() != 0) return

            shared.idleCloseJob = scope.launch {
                delay(IDLE_CONNECTION_TIMEOUT_MS.milliseconds)
                closeIfIdle(key, shared)
            }
        }
    }

    private suspend fun closeIfIdle(key: SessionKey, shared: SharedConnection) {
        val shouldClose = sessionsMutex.withLock {
            if (shared.leases != 0 || sessions[key] !== shared) {
                false
            } else {
                sessions.remove(key)
                true
            }
        }

        if (shouldClose) {
            runCatching { shared.connection.disconnect() }
                .onFailure { Timber.w(it, "Failed to close idle SSH connection") }
        }
    }

    private suspend fun createConnection(
        config: SshConfig,
        auth: SshAuth,
    ): Result<SshConnection> = withContext(ioDispatcher) {
        Timber.i("SSH connect started: %s@%s:%d", config.username, config.host, config.port)
        runCatching {
            val trustedHostKey = trustedHostKey(config.host, config.port)
            val explicitFingerprint = normalizeFingerprint(config.expectedFingerprint)
            val cachedFingerprint = normalizeFingerprint(
                appStorage.getString(StorageDomain.SSH_TRUST, trustedHostKey)
            )
            val fingerprintToVerify = explicitFingerprint ?: cachedFingerprint
            val verifier = InternalHostKeyVerifier(fingerprintToVerify)

            val client = SSHClient()
            client.addHostKeyVerifier(verifier)
            client.connectTimeout = CONNECT_TIMEOUT_MS
            client.timeout = SOCKET_TIMEOUT_MS
            client.connect(config.host, config.port)
            client.connection.timeoutMs = SOCKET_TIMEOUT_MS

            when (auth) {
                is SshAuth.Password -> client.authPassword(config.username, auth.password)
                is SshAuth.PrivateKey -> {
                    val keyProvider = client.loadKeys(auth.content, auth.passphrase?.toCharArray())
                    client.authPublickey(config.username, keyProvider)
                }
            }

            verifier.verifiedFingerprint?.let { verified ->
                if (cachedFingerprint != verified) {
                    appStorage.putString(StorageDomain.SSH_TRUST, trustedHostKey, verified)
                    Timber.i("Stored trusted fingerprint for %s:%d", config.host, config.port)
                }
            }

            Timber.i("SSH connect success: %s@%s:%d", config.username, config.host, config.port)
            SshjConnection(client, ioDispatcher)
        }.onFailure { error ->
            Timber.e(
                error,
                "SSH connect failed: %s@%s:%d",
                config.username,
                config.host,
                config.port
            )
        }
    }

    private fun normalizeFingerprint(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun trustedHostKey(host: String, port: Int): String {
        return "$TRUSTED_HOST_PREFIX${host.lowercase(Locale.ROOT)}:$port"
    }

    private class InternalHostKeyVerifier(
        private val expectedFingerprint: String?
    ) : HostKeyVerifier {

        var verifiedFingerprint: String? = null
            private set

        override fun verify(
            hostname: String,
            port: Int,
            key: PublicKey
        ): Boolean {
            val actualFingerprint = SecurityUtils.getFingerprint(key)

            return when (expectedFingerprint) {
                null -> {
                    Timber.w(
                        "Untrusted host key: %s:%d fingerprint=%s",
                        hostname,
                        port,
                        actualFingerprint
                    )
                    throw SshUntrustedHostException(actualFingerprint)
                }

                actualFingerprint -> {
                    verifiedFingerprint = actualFingerprint
                    true
                }

                else -> {
                    Timber.w(
                        "Host fingerprint mismatch: %s:%d expected=%s actual=%s",
                        hostname,
                        port,
                        expectedFingerprint,
                        actualFingerprint
                    )
                    throw SshUntrustedHostException(actualFingerprint)
                }
            }
        }

        override fun findExistingAlgorithms(
            hostname: String,
            port: Int
        ): List<String?>? {
            return null
        }
    }

    private companion object {
        const val TRUSTED_HOST_PREFIX = "ssh.trusted-host."
        const val IDLE_CONNECTION_TIMEOUT_MS = 120_000L
        const val CONNECT_TIMEOUT_MS = 30_000
        const val SOCKET_TIMEOUT_MS = 30_000
    }
}
