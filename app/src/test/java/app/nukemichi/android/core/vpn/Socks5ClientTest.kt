package app.nukemichi.android.core.vpn

import app.nukemichi.android.core.vpn.internal.Socks5Client
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.DataInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * [Socks5Client] hand-rolls RFC 1928/1929 byte layouts — length prefixes, a big-endian port, and
 * offsets computed against a hostname of arbitrary length. Nothing else in the app re-derives those
 * offsets, so a mistake in them is invisible until a probe silently reports a healthy tunnel as
 * broken (or the reverse).
 *
 * The server below therefore asserts on the exact bytes the client wrote, rather than merely
 * completing a handshake — a client that sent a malformed-but-parseable request would still pass
 * the latter.
 */
class Socks5ClientTest {

    private var server: RecordingSocksServer? = null

    @After
    fun tearDown() {
        server?.close()
    }

    @Test
    fun `offers no-auth and connects when no credentials are given`() {
        val socks = start(RecordingSocksServer())

        socks.connectClient(username = null, password = null, host = "gstatic.com", port = 443)

        assertArrayEquals(
            "greeting must offer exactly one method: no-auth",
            byteArrayOf(0x05, 0x01, 0x00),
            socks.greeting(),
        )
        assertEquals("no credentials means no RFC 1929 exchange", null, socks.authRequestOrNull())
    }

    @Test
    fun `offers username-password and sends an RFC 1929 exchange when credentials are given`() {
        val socks = start(RecordingSocksServer())

        socks.connectClient(username = "nukemichi", password = "s3cret", host = "gstatic.com", port = 443)

        assertArrayEquals(
            "greeting must offer exactly one method: username/password",
            byteArrayOf(0x05, 0x01, 0x02),
            socks.greeting(),
        )
        assertArrayEquals(
            "RFC 1929: subnegotiation version, then each credential length-prefixed",
            byteArrayOf(0x01, 9) + "nukemichi".toByteArray() + byteArrayOf(6) + "s3cret".toByteArray(),
            socks.authRequestOrNull(),
        )
    }

    @Test
    fun `encodes the connect request with a length-prefixed host and a big-endian port`() {
        val socks = start(RecordingSocksServer())

        socks.connectClient(username = null, password = null, host = "example.com", port = 443)

        assertArrayEquals(
            byteArrayOf(0x05, 0x01, 0x00, 0x03, 11) +
                "example.com".toByteArray() +
                byteArrayOf(0x01, 0xBB.toByte()), // 443, high byte first
            socks.connectRequest(),
        )
    }

    /** A port above 0x7FFF is where a sign-extension slip in the shift would show up. */
    @Test
    fun `encodes a high port without sign extension`() {
        val socks = start(RecordingSocksServer())

        socks.connectClient(username = null, password = null, host = "a.io", port = 65535)

        val request = socks.connectRequest()
        assertArrayEquals(
            byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
            request.copyOfRange(request.size - 2, request.size),
        )
    }

    @Test
    fun `fails when the server selects a method the client did not offer`() {
        val socks = start(RecordingSocksServer(methodReply = 0xFF.toByte()))

        assertThrows(IllegalStateException::class.java) {
            socks.connectClient(username = null, password = null, host = "a.io", port = 80)
        }
    }

    @Test
    fun `fails when the server rejects the credentials`() {
        val socks = start(RecordingSocksServer(authStatus = 0x01))

        assertThrows(IllegalStateException::class.java) {
            socks.connectClient(username = "u", password = "p", host = "a.io", port = 80)
        }
    }

    @Test
    fun `fails when the server refuses the connect request`() {
        // 0x05 — connection refused by ruleset, a reply xray returns for a blocked destination.
        val socks = start(RecordingSocksServer(connectStatus = 0x05))

        assertThrows(IllegalStateException::class.java) {
            socks.connectClient(username = null, password = null, host = "a.io", port = 80)
        }
    }

    /**
     * Whether a hang-up surfaces as EOF on the read or a reset on the write depends on which side
     * wins the race, so this pins the contract XrayHealthWatchdog actually relies on — some
     * IOException — rather than a specific subclass it would be flaky to demand.
     */
    @Test
    fun `fails with an IOException when the server hangs up mid-handshake`() {
        val socks = start(RecordingSocksServer(hangUpImmediately = true))

        assertThrows(IOException::class.java) {
            socks.connectClient(username = null, password = null, host = "a.io", port = 80)
        }
    }

    private fun start(socksServer: RecordingSocksServer): RecordingSocksServer =
        socksServer.also { server = it }
}

/**
 * A SOCKS5 server that records what the client sent and replies with configurable status codes.
 * Records rather than validates, so each test asserts only the bytes it cares about.
 */
private class RecordingSocksServer(
    private val methodReply: Byte? = null,
    private val authStatus: Byte = 0x00,
    private val connectStatus: Byte = 0x00,
    private val hangUpImmediately: Boolean = false,
) {
    private val serverSocket = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
    private val greetings = ArrayBlockingQueue<ByteArray>(1)
    private val authRequests = ArrayBlockingQueue<ByteArray>(1)
    private val connectRequests = ArrayBlockingQueue<ByteArray>(1)
    private val noAuth = ByteArray(0)

    private val thread = Thread({
        runCatching {
            serverSocket.accept().use(::handle)
        }
    }, "recording-socks-server").apply { isDaemon = true; start() }

    private fun handle(client: Socket) {
        if (hangUpImmediately) return

        val input = DataInputStream(client.getInputStream())
        val output = client.getOutputStream()

        val greeting = ByteArray(3).also(input::readFully)
        greetings.put(greeting)
        val selected = methodReply ?: greeting[2]
        output.write(byteArrayOf(0x05, selected))
        output.flush()
        if (methodReply != null) return

        if (greeting[2] == 0x02.toByte()) {
            // version, then two length-prefixed fields — read them by their own declared lengths so
            // a wrong length written by the client shows up as a wrong recording, not a hang.
            val version = input.readByte()
            val user = ByteArray(input.readUnsignedByte()).also(input::readFully)
            val pass = ByteArray(input.readUnsignedByte()).also(input::readFully)
            authRequests.put(
                byteArrayOf(version, user.size.toByte()) + user + byteArrayOf(pass.size.toByte()) + pass
            )
            output.write(byteArrayOf(0x01, authStatus))
            output.flush()
            if (authStatus != 0x00.toByte()) return
        } else {
            authRequests.put(noAuth)
        }

        val header = ByteArray(4).also(input::readFully)
        val hostLength = input.readUnsignedByte()
        val host = ByteArray(hostLength).also(input::readFully)
        val port = ByteArray(2).also(input::readFully)
        connectRequests.put(header + byteArrayOf(hostLength.toByte()) + host + port)

        output.write(byteArrayOf(0x05, connectStatus, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
        output.flush()
    }

    fun connectClient(username: String?, password: String?, host: String, port: Int) {
        Socket().use { socket ->
            socket.connect(java.net.InetSocketAddress(serverSocket.inetAddress, serverSocket.localPort), TIMEOUT_MS)
            socket.soTimeout = TIMEOUT_MS
            Socks5Client.connect(socket, username, password, host, port)
        }
    }

    fun greeting(): ByteArray = take(greetings, "greeting")

    fun authRequestOrNull(): ByteArray? = take(authRequests, "auth request").takeIf { it.isNotEmpty() }

    fun connectRequest(): ByteArray = take(connectRequests, "connect request")

    private fun take(queue: ArrayBlockingQueue<ByteArray>, what: String): ByteArray =
        queue.poll(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            ?: error("server never recorded a $what")

    fun close() {
        runCatching { serverSocket.close() }
        thread.interrupt()
    }

    private companion object {
        const val TIMEOUT_MS = 5_000
    }
}
