package io.thernal.firebasekit.firebase.config.impl.data

import io.thernal.firebasekit.firebase.bridge.RemoteConfigBridge
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private class FakeBridge(
    private val result: Map<String, String>?,
) : RemoteConfigBridge {
    var configured: Pair<Long, Long>? = null

    override fun configure(
        minimumFetchIntervalSeconds: Long,
        fetchTimeoutSeconds: Long,
    ) {
        configured = minimumFetchIntervalSeconds to fetchTimeoutSeconds
    }

    override fun activatedValues(): Map<String, String> {
        return mapOf("a" to "1")
    }

    override fun fetchAndActivate(onComplete: (values: Map<String, String>?, error: String?) -> Unit) {
        onComplete(result, "quota exceeded".takeIf { result == null })
    }
}

class BridgeConfigSourceTest {
    @Test
    fun `the first fetch configures the bridge and returns its values`() {
        runTest {
            val bridge = FakeBridge(result = mapOf("b" to "2"))
            val source = BridgeConfigSource(
                minimumFetchInterval = 1.hours,
                fetchTimeout = 1.minutes,
                bridge = { bridge },
            )

            assertEquals(mapOf("b" to "2"), source.fetch())
            assertEquals(3600L to 60L, bridge.configured)
            assertEquals(mapOf("a" to "1"), source.activated())
        }
    }

    @Test
    fun `a bridge error is a failed fetch`() {
        runTest {
            val source = BridgeConfigSource(
                minimumFetchInterval = 1.hours,
                fetchTimeout = 1.minutes,
                bridge = { FakeBridge(result = null) },
            )

            assertFailsWith<IllegalStateException> { source.fetch() }
        }
    }

    @Test
    fun `without a bridge nothing is activated and fetching fails`() {
        runTest {
            val source = BridgeConfigSource(minimumFetchInterval = 1.hours, fetchTimeout = 1.minutes, bridge = { null })

            assertEquals(emptyMap(), source.activated())
            assertFailsWith<IllegalStateException> { source.fetch() }
        }
    }
}
