package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.internal.XrayHealthWatchdog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Drives [XrayHealthWatchdog] against a real loopback [ServerSocket] standing in for xray-core's
 * local SOCKS5 inbound, over the same probe path it exercises against the genuine one, rather than
 * faking the socket layer, since the class's whole reason to exist is trusting nothing but a raw
 * socket. Probe cadence (15s) and the consecutive-failure threshold (2 probes) come straight from
 * the production constants, so the test drives them through [kotlinx.coroutines.test]'s virtual
 * clock instead of actually waiting on them.
 */
class XrayHealthWatchdogTest {

    private var server: FakeSocksServer? = null

    @After
    fun tearDown() {
        server?.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `does not report degraded while probes keep succeeding`() = runTest {
        val socksServer = FakeSocksServer(FakeSocksServer.Behavior.ACCEPT).also { server = it }
        val watchdog = XrayHealthWatchdog(StandardTestDispatcher(testScheduler))
        var degradedCount = 0

        watchdog.start(this, socksServer.endpoint()) { degradedCount++ }

        // Three probe intervals' worth of healthy probes.
        advanceTimeBy(3 * PROBE_INTERVAL_MS)
        runCurrent()

        assertEquals(0, degradedCount)
        assertTrue("a healthy target should see at least one probe land", socksServer.connectionCount() > 0)
        watchdog.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `reports degraded once after two consecutive failed probes`() = runTest {
        val socksServer = FakeSocksServer(FakeSocksServer.Behavior.HANG_UP).also { server = it }
        val watchdog = XrayHealthWatchdog(StandardTestDispatcher(testScheduler))
        var degradedCount = 0

        watchdog.start(this, socksServer.endpoint()) { degradedCount++ }

        // First failed probe alone must not trip it.
        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(0, degradedCount)

        // Second consecutive failure crosses the threshold.
        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(1, degradedCount)

        // The watchdog stops probing itself once degraded is reported, so no further calls.
        advanceTimeBy(5 * PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(1, degradedCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a healthy probe resets the failure streak`() = runTest {
        val socksServer = FakeSocksServer(FakeSocksServer.Behavior.HANG_UP).also { server = it }
        val watchdog = XrayHealthWatchdog(StandardTestDispatcher(testScheduler))
        var degradedCount = 0

        watchdog.start(this, socksServer.endpoint()) { degradedCount++ }

        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(0, degradedCount)

        socksServer.behavior = FakeSocksServer.Behavior.ACCEPT
        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(0, degradedCount)

        socksServer.behavior = FakeSocksServer.Behavior.HANG_UP
        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(0, degradedCount) // one failure after a reset streak still isn't two in a row

        advanceTimeBy(PROBE_INTERVAL_MS)
        runCurrent()
        assertEquals(1, degradedCount)
    }

    private companion object {
        const val PROBE_INTERVAL_MS = 15_000L
    }
}

/**
 * A minimal loopback SOCKS5 server. [Behavior.ACCEPT] completes the same no-auth negotiation and
 * CONNECT reply [XrayHealthWatchdog]'s probe expects from a healthy xray-core; [Behavior.HANG_UP]
 * closes the connection the instant it's accepted, which is what a stuck/blackholed dial pool
 * looks like from the probing socket's side: no reply, the connection just dies. It costs none of
 * the real probe timeout in test time.
 */
private class FakeSocksServer(@Volatile var behavior: Behavior) {
    enum class Behavior { ACCEPT, HANG_UP }

    private val serverSocket = ServerSocket(0, 50, java.net.InetAddress.getLoopbackAddress())
    private val accepted = java.util.concurrent.atomic.AtomicInteger(0)
    private val thread = Thread({
        try {
            while (!serverSocket.isClosed) {
                val client = serverSocket.accept()
                accepted.incrementAndGet()
                Thread({ handle(client) }, "fake-socks-handler").apply { isDaemon = true }.start()
            }
        } catch (_: Exception) {
            // Expected once close() tears down the listening socket.
        }
    }, "fake-socks-server").apply { isDaemon = true; start() }

    private fun handle(client: Socket) {
        client.use {
            when (behavior) {
                Behavior.HANG_UP -> return
                Behavior.ACCEPT -> {
                    val input = DataInputStream(client.getInputStream())
                    val output = client.getOutputStream()

                    val greeting = ByteArray(3)
                    input.readFully(greeting) // version, nmethods, no-auth method
                    output.write(byteArrayOf(0x05, 0x00))
                    output.flush()

                    val header = ByteArray(4)
                    input.readFully(header) // version, CONNECT, reserved, address type
                    if (header[3].toInt() == 0x03) {
                        val addressLength = input.readUnsignedByte()
                        input.readFully(ByteArray(addressLength))
                    }
                    input.readFully(ByteArray(2)) // port
                    output.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
                    output.flush()
                }
            }
        }
    }

    fun endpoint() = SocksEndpoint(
        host = serverSocket.inetAddress.hostAddress ?: "127.0.0.1",
        port = serverSocket.localPort,
    )

    fun connectionCount() = accepted.get()

    fun close() {
        runCatching { serverSocket.close() }
        thread.interrupt()
    }
}
