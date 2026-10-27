package io.thernal.firebasekit.firebase.database.testing

import io.thernal.firebasekit.firebase.database.api.data.DatabaseException
import io.thernal.firebasekit.firebase.database.api.data.RealtimeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * A database a test drives, held in memory as one JSON tree. Writes apply at once and every observer of a
 * changed path sees the new value. [deny] makes the rules refuse a path and everything below it; `ServerValue`s
 * are resolved with [now]. As in Firebase, a node left with no children is removed; arrays are kept as written.
 */
class FakeRealtimeDatabase(
    private val now: () -> Long = { 0L },
) : RealtimeDatabase {
    private val root = MutableStateFlow<JsonElement>(JsonNull)
    private val denied = mutableSetOf<List<String>>()
    private var keys = 0

    fun deny(path: String) {
        denied += segments(path)
    }

    fun allow(path: String) {
        denied -= segments(path)
    }

    override fun newKey(): String {
        keys += 1
        return "-key" + keys.toString().padStart(KEY_DIGITS, '0')
    }

    override suspend fun set(
        path: String,
        value: JsonElement,
    ) {
        val at = checked(path)
        root.update { tree -> tree.with(path = at, value = value.resolved(current = tree.at(at))) }
    }

    override suspend fun update(
        path: String,
        values: Map<String, JsonElement>,
    ) {
        val at = checked(path)
        val children = values.mapKeys { (child, _) -> checked(at + segments(child)) }
        root.update { tree ->
            children.entries.fold(tree) { result, (child, value) ->
                result.with(path = child, value = value.resolved(current = result.at(child)))
            }
        }
    }

    override suspend fun remove(path: String) {
        set(path = path, value = JsonNull)
    }

    override suspend fun get(path: String): JsonElement? {
        return root.value.at(checked(path))
    }

    override fun observe(path: String): Flow<JsonElement?> {
        return flow {
            val at = checked(path)
            emitAll(root.map { tree -> tree.at(at) }.distinctUntilChanged())
        }
    }

    private fun checked(path: String): List<String> {
        return checked(segments(path))
    }

    private fun checked(at: List<String>): List<String> {
        if (denied.any { refused -> at.take(refused.size) == refused }) {
            throw DatabaseException(message = "Permission denied: /${at.joinToString("/")}", isPermissionDenied = true)
        }
        return at
    }

    private fun JsonElement.resolved(current: JsonElement?): JsonElement {
        if (this !is JsonObject) {
            return this
        }
        val server = this[SERVER_VALUE]
            ?: return JsonObject(mapValues { (key, value) -> value.resolved(current = current?.at(listOf(key))) })
        val increment = (server as? JsonObject)?.get(INCREMENT)?.jsonPrimitive?.longOrNull
        return if (increment != null) {
            JsonPrimitive((current as? JsonPrimitive)?.longOrNull?.plus(increment) ?: increment)
        } else {
            JsonPrimitive(now())
        }
    }
}

private fun segments(path: String): List<String> {
    return path.split('/').filter { segment -> segment.isNotEmpty() }
}

private fun JsonElement.at(path: List<String>): JsonElement? {
    val node = path.fold<String, JsonElement?>(this) { node, key -> (node as? JsonObject)?.get(key) }
    return node.takeUnless { it == null || it == JsonNull }
}

private fun JsonElement.with(
    path: List<String>,
    value: JsonElement,
): JsonElement {
    if (path.isEmpty()) {
        return value.pruned()
    }
    val children = (this as? JsonObject).orEmpty()
    val child = (children[path.first()] ?: JsonNull).with(path = path.drop(1), value = value)
    val updated = if (child == JsonNull) {
        children - path.first()
    } else {
        children + (path.first() to child)
    }
    return if (updated.isEmpty()) {
        JsonNull
    } else {
        JsonObject(updated)
    }
}

private fun JsonElement.pruned(): JsonElement {
    if (this !is JsonObject) {
        return this
    }
    val children = mapValues { (_, child) -> child.pruned() }.filterValues { child -> child != JsonNull }
    return if (children.isEmpty()) {
        JsonNull
    } else {
        JsonObject(children)
    }
}

private const val KEY_DIGITS = 8
private const val SERVER_VALUE = ".sv"
private const val INCREMENT = "increment"
