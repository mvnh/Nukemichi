package app.nukemichi.android.core.vpn

import kotlinx.serialization.json.Json

object XrayJson {
    val default: Json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = false
    }
}
