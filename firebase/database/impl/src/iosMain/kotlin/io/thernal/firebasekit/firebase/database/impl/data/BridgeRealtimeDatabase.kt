package io.thernal.firebasekit.firebase.database.impl.data

import io.thernal.firebasekit.firebase.bridge.DatabaseBridge
import io.thernal.firebasekit.firebase.bridge.DatabaseBridgeError
import io.thernal.firebasekit.firebase.bridge.FirebaseBridges
import io.thernal.firebasekit.firebase.database.api.data.DatabaseException
import io.thernal.firebasekit.firebase.database.api.data.RealtimeDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun platformRealtimeDatabase(): RealtimeDatabase {
    return BridgeRealtimeDatabase { FirebaseBridges.database }
}

/** The Realtime Database through the app's Swift bridge; JSON crosses it as text. */
internal class BridgeRealtimeDatabase(
    private val bridge: () -> DatabaseBridge?,
) : RealtimeDatabase {
    override fun newKey(): String {
        return database().pushKey()
    }

    override suspend fun set(
        path: String,
        value: JsonElement,
    ) {
        val database = database()
        write { onComplete -> database.set(path = path, json = value.toString(), onComplete = onComplete) }
    }

    override suspend fun update(
        path: String,
        values: Map<String, JsonElement>,
    ) {
        val database = database()
        val json = JsonObject(values).toString()
        write { onComplete -> database.update(path = path, json = json, onComplete = onComplete) }
    }

    override suspend fun remove(path: String) {
        val database = database()
        write { onComplete -> database.remove(path = path, onComplete = onComplete) }
    }

    override suspend fun get(path: String): JsonElement? {
        val database = database()
        return suspendCancellableCoroutine { continuation ->
            database.get(path = path) { json, error ->
                if (error == null) {
                    continuation.resume(json?.let(Json::parseToJsonElement))
                } else {
                    continuation.resumeWithException(error.toDatabaseException())
                }
            }
        }
    }

    override fun observe(path: String): Flow<JsonElement?> {
        return callbackFlow {
            val observation = database().observe(
                path = path,
                onValue = { json -> trySend(json?.let(Json::parseToJsonElement)) },
                onError = { error -> close(error.toDatabaseException()) },
            )
            awaitClose { observation.cancel() }
        }
    }

    private fun database(): DatabaseBridge {
        return bridge()
            ?: throw DatabaseException(message = "The database bridge is not registered", isPermissionDenied = false)
    }

    private suspend fun write(start: (onComplete: (DatabaseBridgeError?) -> Unit) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            start { error ->
                if (error == null) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(error.toDatabaseException())
                }
            }
        }
    }

    private fun DatabaseBridgeError.toDatabaseException(): DatabaseException {
        return DatabaseException(message = message, isPermissionDenied = isPermissionDenied)
    }
}
