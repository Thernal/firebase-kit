package io.thernal.firebasekit.firebase.crash.impl.data

import io.thernal.firebasekit.firebase.bridge.CrashlyticsBridge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingBridge : CrashlyticsBridge {
    val calls = mutableListOf<String>()

    override fun setCollectionEnabled(enabled: Boolean) {
        calls += "enabled=$enabled"
    }

    override fun setUserId(userId: String) {
        calls += "user=$userId"
    }

    override fun log(message: String) {
        calls += "log=$message"
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        calls += "$key=$value"
    }

    override fun recordError(
        type: String,
        message: String,
        stackTrace: String,
    ) {
        calls += "error=$type:$message"
        check(stackTrace.isNotEmpty())
    }
}

class BridgeCrashReporterTest {
    @Test
    fun `calls reach the bridge and a cleared user is empty`() {
        val bridge = RecordingBridge()
        val reporter = BridgeCrashReporter { bridge }

        reporter.setCollectionEnabled(false)
        reporter.setUserId(null)
        reporter.log("opened inbox")
        reporter.setCustomKey(key = "flavor", value = "beta")
        reporter.recordException(IllegalStateException("broken"))

        assertEquals(
            listOf("enabled=false", "user=", "log=opened inbox", "flavor=beta", "error=IllegalStateException:broken"),
            bridge.calls,
        )
    }

    @Test
    fun `without a registered bridge nothing happens`() {
        val reporter = BridgeCrashReporter { null }

        reporter.recordException(IllegalStateException())

        assertTrue(true)
    }
}
