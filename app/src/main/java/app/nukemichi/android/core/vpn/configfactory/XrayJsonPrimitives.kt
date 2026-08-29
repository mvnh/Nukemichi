package app.nukemichi.android.core.vpn.configfactory

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun intPrimitive(value: Int): JsonPrimitive = JsonPrimitive(value)

internal fun jsonObjectOf(vararg pairs: Pair<String, Any>): JsonObject = JsonObject(
    pairs.associate { (key, value) ->
        key to when (value) {
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            else -> JsonPrimitive(value.toString())
        }
    },
)
