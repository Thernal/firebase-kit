package io.thernal.firebasekit.firebase.database.api.data

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

/**
 * The app's default Realtime Database, read and written as JSON with the signed-in Firebase user's
 * rights. A path is slash-separated from the root (`users/ada/settings`); `""` is the root.
 *
 * A write completes when the server has it. Offline it waits for the connection — the SDK keeps it and
 * sends it later — so a caller that has to answer the user bounds it with a timeout.
 */
interface RealtimeDatabase {
    /** A child key made on the device, unique and ordered by creation time — what `push()` names a child. */
    fun newKey(): String

    /** Replaces the value at [path]; `JsonNull` removes it. [ServerValue]s anywhere inside are resolved by the server. */
    suspend fun set(
        path: String,
        value: JsonElement,
    )

    /**
     * Writes every entry of [values] at once, each key a path relative to [path] (`"profile/name"`); a `JsonNull`
     * removes that child. Either all of them are written or none.
     */
    suspend fun update(
        path: String,
        values: Map<String, JsonElement>,
    )

    suspend fun remove(path: String)

    /** The value at [path] from the server, or the cached one when offline; `null` when there is none. */
    suspend fun get(path: String): JsonElement?

    /**
     * The value at [path] now and after every change, `null` while there is none. Ends with
     * [DatabaseException] when the rules refuse the read, then or later.
     */
    fun observe(path: String): Flow<JsonElement?>
}
