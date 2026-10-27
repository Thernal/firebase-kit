package io.thernal.firebasekit.firebase.database.impl.data

import io.thernal.firebasekit.firebase.bridge.DatabaseBridge
import io.thernal.firebasekit.firebase.bridge.DatabaseBridgeError
import io.thernal.firebasekit.firebase.bridge.DatabaseObservation
import io.thernal.firebasekit.firebase.database.api.data.DatabaseException
import io.thernal.firebasekit.firebase.database.api.data.ServerValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeBridge(
    private val error: DatabaseBridgeError? = null,
    private val values: List<String?> = emptyList(),
) : DatabaseBridge {
    val writes = mutableListOf<String>()
    var isCancelled = false

    override fun pushKey(): String {
        return "-key"
    }

    override fun set(
        path: String,
        json: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    ) {
        writes += "set $path $json"
        onComplete(error)
    }

    override fun update(
        path: String,
        json: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    ) {
        writes += "update $path $json"
        onComplete(error)
    }

    override fun remove(
        path: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    ) {
        writes += "remove $path"
        onComplete(error)
    }

    override fun get(
        path: String,
        onComplete: (json: String?, error: DatabaseBridgeError?) -> Unit,
    ) {
        onComplete(values.firstOrNull(), error)
    }

    override fun observe(
        path: String,
        onValue: (json: String?) -> Unit,
        onError: (error: DatabaseBridgeError) -> Unit,
    ): DatabaseObservation {
        values.forEach(onValue)
        error?.let(onError)
        return object : DatabaseObservation {
            override fun cancel() {
                isCancelled = true
            }
        }
    }
}

class BridgeRealtimeDatabaseTest {
    @Test
    fun `writes cross the bridge as JSON text`() {
        runTest {
            val bridge = FakeBridge()
            val database = BridgeRealtimeDatabase { bridge }

            database.set(path = "a/b", value = buildJsonObject { put("at", ServerValue.Timestamp) })
            database.update(path = "a", values = mapOf("b/c" to JsonPrimitive(1), "d" to JsonNull))
            database.remove(path = "a")

            assertEquals(
                listOf("set a/b {\"at\":{\".sv\":\"timestamp\"}}", "update a {\"b/c\":1,\"d\":null}", "remove a"),
                bridge.writes,
            )
        }
    }

    @Test
    fun `a value is parsed and no value is null`() {
        runTest {
            assertEquals(JsonPrimitive(true), BridgeRealtimeDatabase { FakeBridge(values = listOf("true")) }.get("a"))
            assertNull(BridgeRealtimeDatabase { FakeBridge(values = listOf(null)) }.get("a"))
        }
    }

    @Test
    fun `a refused write is a permission-denied failure`() {
        runTest {
            val database = BridgeRealtimeDatabase {
                FakeBridge(error = DatabaseBridgeError(message = "Permission Denied", isPermissionDenied = true))
            }

            val failure = assertFailsWith<DatabaseException> { database.set(path = "a", value = JsonPrimitive(1)) }

            assertTrue(failure.isPermissionDenied)
        }
    }

    @Test
    fun `without a registered bridge every call fails`() {
        runTest {
            val database = BridgeRealtimeDatabase { null }

            assertFailsWith<DatabaseException> { database.get("a") }
            assertFailsWith<DatabaseException> { database.observe("a").first() }
        }
    }

    @Test
    fun `an observation emits every value and detaches when collection ends`() {
        runTest {
            val bridge = FakeBridge(values = listOf("1", null))

            val values = BridgeRealtimeDatabase { bridge }.observe("a").take(2).toList()

            assertEquals(listOf(JsonPrimitive(1), null), values)
            assertTrue(bridge.isCancelled)
        }
    }

    @Test
    fun `a cancelled observation ends the flow with the failure`() {
        runTest {
            val database = BridgeRealtimeDatabase {
                FakeBridge(error = DatabaseBridgeError(message = "Permission Denied", isPermissionDenied = true))
            }

            val failure = assertFailsWith<DatabaseException> { database.observe("a").toList() }

            assertTrue(failure.isPermissionDenied)
        }
    }
}
