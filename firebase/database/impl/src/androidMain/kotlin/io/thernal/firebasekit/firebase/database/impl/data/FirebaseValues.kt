package io.thernal.firebasekit.firebase.database.impl.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

// The Android SDK reads and writes plain Java values — maps, lists, strings, longs, doubles, booleans — so
// JSON crosses it as those. A map with a ".sv" key is a server value to the SDK, which is what ServerValue is.

internal fun JsonElement.toFirebaseValue(): Any? {
    return when (this) {
        is JsonObject -> mapValues { (_, value) -> value.toFirebaseValue() }

        is JsonArray -> map { value -> value.toFirebaseValue() }

        JsonNull -> null

        is JsonPrimitive -> if (isString) {
            content
        } else {
            booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
        }
    }
}

internal fun Any.toJsonElement(): JsonElement {
    return when (this) {
        // The database's keys are strings; nothing else names a child.
        is Map<*, *> -> JsonObject(
            entries
                .mapNotNull { (key, value) ->
                    val name = key as? String ?: return@mapNotNull null
                    name to (value?.toJsonElement() ?: JsonNull)
                }
                .toMap(),
        )

        is List<*> -> JsonArray(map { value -> value?.toJsonElement() ?: JsonNull })

        is Boolean -> JsonPrimitive(this)

        is Number -> JsonPrimitive(this)

        else -> JsonPrimitive(toString())
    }
}
