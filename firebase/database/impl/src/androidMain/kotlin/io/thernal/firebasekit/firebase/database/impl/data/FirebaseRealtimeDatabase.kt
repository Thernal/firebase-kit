package io.thernal.firebasekit.firebase.database.impl.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.thernal.firebasekit.firebase.database.api.data.DatabaseException
import io.thernal.firebasekit.firebase.database.api.data.RealtimeDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun platformRealtimeDatabase(): RealtimeDatabase {
    return FirebaseRealtimeDatabase
}

/** The default database of google-services.json (`firebase_url`), as the FirebaseAuth user. */
internal object FirebaseRealtimeDatabase : RealtimeDatabase {
    private val database: FirebaseDatabase
        get() {
            return try {
                FirebaseDatabase.getInstance()
            } catch (notConfigured: IllegalStateException) {
                throw DatabaseException(
                    message = "Firebase is not configured",
                    isPermissionDenied = false,
                    cause = notConfigured,
                )
            }
        }

    override fun newKey(): String {
        return checkNotNull(database.reference.push().key) { "The database gave no key to push under" }
    }

    override suspend fun set(
        path: String,
        value: JsonElement,
    ) {
        write { listener -> database.getReference(path).setValue(value.toFirebaseValue(), listener) }
    }

    override suspend fun update(
        path: String,
        values: Map<String, JsonElement>,
    ) {
        val children = values.mapValues { (_, value) -> value.toFirebaseValue() }
        write { listener -> database.getReference(path).updateChildren(children, listener) }
    }

    override suspend fun remove(path: String) {
        write { listener -> database.getReference(path).removeValue(listener) }
    }

    override suspend fun get(path: String): JsonElement? {
        val snapshot = try {
            database.getReference(path).get().await()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (
            // get() fails with a plain Exception carrying the server's message, not a DatabaseError.
            @Suppress("TooGenericExceptionCaught") failure: Exception,
        ) {
            val message = failure.message ?: "The read failed"
            throw DatabaseException(
                message = message,
                isPermissionDenied = message.contains(other = PERMISSION_DENIED, ignoreCase = true),
                cause = failure,
            )
        }
        return snapshot.value?.toJsonElement()
    }

    override fun observe(path: String): Flow<JsonElement?> {
        return callbackFlow {
            val reference = database.getReference(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot.value?.toJsonElement())
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toDatabaseException())
                }
            }
            reference.addValueEventListener(listener)
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    private suspend fun write(start: (DatabaseReference.CompletionListener) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            start { error, _ ->
                if (error == null) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(error.toDatabaseException())
                }
            }
        }
    }

    private fun DatabaseError.toDatabaseException(): DatabaseException {
        return DatabaseException(message = message, isPermissionDenied = code == DatabaseError.PERMISSION_DENIED)
    }
}

private const val PERMISSION_DENIED = "permission denied"
