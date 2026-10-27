package io.thernal.firebasekit.firebase.database.testing

import io.thernal.firebasekit.firebase.database.api.data.DatabaseException
import io.thernal.firebasekit.firebase.database.api.data.ServerValue
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeRealtimeDatabaseTest {
    @Test
    fun `a set value is read back at its path and its parents`() {
        runTest {
            val database = FakeRealtimeDatabase(now = { NOW })

            database.set(path = "users/ada", value = buildJsonObject { put("seen", ServerValue.Timestamp) })

            assertEquals(Json.parseToJsonElement("""{"seen":$NOW}"""), database.get("/users/ada/"))
            assertEquals(JsonPrimitive(NOW), database.get("users/ada/seen"))
            assertNull(database.get("users/bob"))
        }
    }

    @Test
    fun `an update writes several paths and a null removes one`() {
        runTest {
            val database = FakeRealtimeDatabase()
            database.set(path = "a", value = Json.parseToJsonElement("""{"b":1,"c":{"d":2}}"""))

            database.update(path = "a", values = mapOf("b" to ServerValue.increment(delta = 2), "c/d" to JsonNull))

            assertEquals(Json.parseToJsonElement("""{"b":3}"""), database.get("a"))
        }
    }

    @Test
    fun `removing the last child removes its parent`() {
        runTest {
            val database = FakeRealtimeDatabase()
            database.set(path = "a/b", value = JsonPrimitive(1))

            database.remove(path = "a/b")

            assertNull(database.get("a"))
        }
    }

    @Test
    fun `an observer sees the value now and every change`() {
        runTest {
            val database = FakeRealtimeDatabase()
            val values = mutableListOf<JsonElement?>()
            val collecting = launch { database.observe("a").take(3).toList(values) }
            testScheduler.runCurrent()

            database.set(path = "a", value = JsonPrimitive(1))
            testScheduler.runCurrent()
            database.remove(path = "a")
            collecting.join()

            assertEquals(listOf<JsonElement?>(null, JsonPrimitive(1), null), values)
        }
    }

    @Test
    fun `a denied path refuses reads and writes below it`() {
        runTest {
            val database = FakeRealtimeDatabase()
            database.deny("admin")

            val write = assertFailsWith<DatabaseException> {
                database.set(path = "admin/settings", value = JsonPrimitive(1))
            }
            assertFailsWith<DatabaseException> { database.observe("admin").toList() }
            database.set(path = "public", value = JsonPrimitive(1))

            assertTrue(write.isPermissionDenied)
        }
    }

    @Test
    fun `new keys are distinct and ordered`() {
        val database = FakeRealtimeDatabase()

        val keys = List(3) { database.newKey() }

        assertEquals(keys.sorted(), keys)
        assertEquals(3, keys.toSet().size)
    }
}

private const val NOW = 1_700_000_000_000L
