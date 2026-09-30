package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessageStream
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow

/** Push a test drives: set [currentToken], call [newToken]; [topics] holds the subscriptions. */
class FakePushMessaging(
    initialToken: String? = "test-token",
) : PushMessaging {
    private val token = MutableStateFlow(initialToken)
    val topics = mutableSetOf<String>()

    override val tokens: Flow<String> = token.filterNotNull()

    fun newToken(value: String) {
        token.value = value
    }

    override suspend fun token(): String {
        return token.value ?: error("No push token")
    }

    override suspend fun subscribe(topic: String) {
        topics += topic
    }

    override suspend fun unsubscribe(topic: String) {
        topics -= topic
    }
}

/** Messages a test delivers with [receive] and [open]. */
class FakePushMessageStream : PushMessageStream {
    private val received = MutableSharedFlow<PushMessage>(extraBufferCapacity = BUFFER)
    private val tapped = Channel<PushMessage>(Channel.UNLIMITED)

    override val messages: Flow<PushMessage> = received.asSharedFlow()
    override val opened: Flow<PushMessage> = tapped.receiveAsFlow()

    fun receive(message: PushMessage) {
        received.tryEmit(message)
    }

    fun open(message: PushMessage) {
        tapped.trySend(message)
    }

    private companion object {
        const val BUFFER = 16
    }
}
