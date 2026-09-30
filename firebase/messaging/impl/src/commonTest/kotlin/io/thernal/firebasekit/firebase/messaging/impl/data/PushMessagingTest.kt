package io.thernal.firebasekit.firebase.messaging.impl.data

import io.thernal.firebasekit.firebase.bridge.PushHub
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage
import io.thernal.firebasekit.firebase.messaging.api.data.pushMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakePlatform(
    var current: String? = "t1",
) : PushPlatform {
    val topics = mutableListOf<String>()

    override suspend fun token(): String {
        return current ?: error("no token yet")
    }

    override suspend fun subscribe(topic: String) {
        topics += "+$topic"
    }

    override suspend fun unsubscribe(topic: String) {
        topics += "-$topic"
    }
}

class PushMessagingTest {
    @Test
    fun `tokens start with the current one and follow refreshes without repeats`() {
        runTest {
            val refreshes = MutableSharedFlow<String>(extraBufferCapacity = 4)
            val messaging = DefaultPushMessaging(platform = FakePlatform(current = "t1"), refreshedTokens = refreshes)
            val seen = mutableListOf<String>()
            val job = launch { messaging.tokens.take(2).toList(seen) }
            runCurrent()

            refreshes.emit("t1")
            refreshes.emit("t2")
            job.join()

            assertEquals(listOf("t1", "t2"), seen)
        }
    }

    @Test
    fun `without a token yet the stream waits for the first refresh`() {
        runTest {
            val refreshes = MutableSharedFlow<String>(extraBufferCapacity = 4)
            val messaging = DefaultPushMessaging(platform = FakePlatform(current = null), refreshedTokens = refreshes)
            val first = launch { assertEquals("apns-ready", messaging.tokens.first()) }
            runCurrent()

            refreshes.emit("apns-ready")
            first.join()
        }
    }

    @Test
    fun `topics go to the platform`() {
        runTest {
            val platform = FakePlatform()
            val messaging = DefaultPushMessaging(platform = platform, refreshedTokens = MutableSharedFlow())

            messaging.subscribe("news")
            messaging.unsubscribe("news")

            assertEquals(listOf("+news", "-news"), platform.topics)
        }
    }

    @Test
    fun `a tapped notification waits until someone collects it`() {
        runTest {
            val message = pushMessage(title = "Hi", body = null, data = mapOf("deepLink" to "app://inbox"))

            PushHub.emitOpened(message)

            assertEquals(message, HubPushMessageStream.opened.first())
        }
    }

    @Test
    fun `the deepLink data key wins over the notification's link and empty texts are absent`() {
        val message: PushMessage = pushMessage(
            title = "",
            body = "Body",
            data = mapOf("deepLink" to "app://a"),
            link = "app://b",
        )

        assertEquals("app://a", message.deepLink)
        assertNull(message.title)
        assertEquals("app://b", pushMessage(title = null, body = null, data = emptyMap(), link = "app://b").deepLink)
    }

    @Test
    fun `swift entry points build messages`() {
        runTest {
            PushHub.open(title = "T", body = "B", data = mapOf("id" to "7"))

            val opened = HubPushMessageStream.opened.first()
            assertEquals(PushMessage(title = "T", body = "B", data = mapOf("id" to "7"), deepLink = null), opened)
        }
    }
}
