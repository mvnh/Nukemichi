package app.nukemichi.android.core.vpn.spec

import kotlinx.serialization.Serializable

@Serializable
enum class XhttpMode(val wireValue: String) {
    STREAM_ONE("stream-one"),
    AUTO("auto"),
    PACKET_UP("packet-up"),
    STREAM_UP("stream-up"),
}
