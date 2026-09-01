package app.nukemichi.android.core.vpn.internal

import java.io.DataInputStream
import java.io.OutputStream
import java.net.Socket

internal object Socks5Client {

    private const val VERSION: Byte = 0x05
    private const val NO_AUTH: Byte = 0x00
    private const val USERNAME_PASSWORD_AUTH: Byte = 0x02
    private const val AUTH_SUBNEGOTIATION_VERSION: Byte = 0x01
    private const val COMMAND_CONNECT: Byte = 0x01
    private const val ADDRESS_TYPE_DOMAIN: Byte = 0x03
    private const val REPLY_SUCCEEDED: Byte = 0x00

    fun connect(socket: Socket, username: String?, password: String?, host: String, port: Int) {
        val out = socket.getOutputStream()
        val input = DataInputStream(socket.getInputStream())
        val useAuth = username != null && password != null

        negotiateAuthMethod(out, input, useAuth)
        if (useAuth) authenticate(out, input, username, password)
        sendConnectRequest(out, input, host, port)
    }

    /** Step 1, greeting: offer one auth method, expect the server to echo it back. */
    private fun negotiateAuthMethod(out: OutputStream, input: DataInputStream, useAuth: Boolean) {
        val offeredMethod = if (useAuth) USERNAME_PASSWORD_AUTH else NO_AUTH
        out.write(byteArrayOf(VERSION, 0x01, offeredMethod))
        out.flush()

        val reply = ByteArray(2)
        input.readFully(reply)
        check(reply[0] == VERSION && reply[1] == offeredMethod) { "SOCKS5 greeting rejected" }
    }

    /** Step 2 (RFC 1929): version byte, then username/password each length-prefixed. */
    private fun authenticate(out: OutputStream, input: DataInputStream, username: String, password: String) {
        val userBytes = username.toByteArray(Charsets.US_ASCII)
        val passBytes = password.toByteArray(Charsets.US_ASCII)
        val request = ByteArray(3 + userBytes.size + passBytes.size)
        request[0] = AUTH_SUBNEGOTIATION_VERSION
        request[1] = userBytes.size.toByte()
        userBytes.copyInto(request, destinationOffset = 2)
        request[2 + userBytes.size] = passBytes.size.toByte()
        passBytes.copyInto(request, destinationOffset = 3 + userBytes.size)
        out.write(request)
        out.flush()

        val reply = ByteArray(2)
        input.readFully(reply)
        check(reply[1] == REPLY_SUCCEEDED) { "SOCKS5 auth rejected" }
    }

    /** Step 3, CONNECT: version, command, reserved byte, then a length-prefixed hostname and the
     *  port. Only checks for success, not which failure; the caller just needs reachable/not. */
    private fun sendConnectRequest(out: OutputStream, input: DataInputStream, host: String, port: Int) {
        val hostBytes = host.toByteArray(Charsets.US_ASCII)
        val request = ByteArray(7 + hostBytes.size)
        request[0] = VERSION
        request[1] = COMMAND_CONNECT
        // request[2] is the reserved byte, left at its default 0x00.
        request[3] = ADDRESS_TYPE_DOMAIN
        request[4] = hostBytes.size.toByte()
        hostBytes.copyInto(request, destinationOffset = 5)
        request[5 + hostBytes.size] = (port shr 8).toByte()
        request[6 + hostBytes.size] = (port and 0xFF).toByte()
        out.write(request)
        out.flush()

        val reply = ByteArray(4)
        input.readFully(reply)
        check(reply[1] == REPLY_SUCCEEDED) { "SOCKS5 CONNECT failed, reply code ${reply[1]}" }
    }
}
