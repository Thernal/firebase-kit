package io.thernal.firebasekit.firebase.database.api.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Values the server fills in when it applies a write — the SDKs' `ServerValue`, as JSON. */
object ServerValue {
    /** The server's clock, in milliseconds since the epoch. */
    val Timestamp: JsonObject = JsonObject(mapOf(SERVER_VALUE to JsonPrimitive("timestamp")))

    /** The stored number plus [delta], applied atomically on the server; a missing value counts as 0. */
    fun increment(delta: Long): JsonObject {
        return JsonObject(mapOf(SERVER_VALUE to JsonObject(mapOf("increment" to JsonPrimitive(delta)))))
    }
}

private const val SERVER_VALUE = ".sv"
