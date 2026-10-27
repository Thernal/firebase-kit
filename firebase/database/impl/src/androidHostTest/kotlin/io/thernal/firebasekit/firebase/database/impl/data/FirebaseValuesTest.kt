package io.thernal.firebasekit.firebase.database.impl.data

import io.thernal.firebasekit.firebase.database.api.data.ServerValue
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseValuesTest {
    @Test
    fun `JSON becomes the SDK's plain values`() {
        val json = Json.parseToJsonElement("""{"s":"1","l":2,"d":2.5,"b":true,"n":null,"a":[1,"x"]}""")

        assertEquals(
            mapOf("s" to "1", "l" to 2L, "d" to 2.5, "b" to true, "n" to null, "a" to listOf(1L, "x")),
            json.toFirebaseValue(),
        )
    }

    @Test
    fun `a server value stays the map the SDK resolves`() {
        assertEquals(mapOf(".sv" to "timestamp"), ServerValue.Timestamp.toFirebaseValue())
        assertEquals(mapOf(".sv" to mapOf("increment" to 3L)), ServerValue.increment(delta = 3).toFirebaseValue())
    }

    @Test
    fun `the SDK's values become JSON`() {
        val value = mapOf("l" to 2L, "d" to 2.5, "b" to false, "a" to listOf("x", null), "m" to mapOf("k" to "v"))

        assertEquals(
            Json.parseToJsonElement("""{"l":2,"d":2.5,"b":false,"a":["x",null],"m":{"k":"v"}}"""),
            value.toJsonElement(),
        )
    }
}
