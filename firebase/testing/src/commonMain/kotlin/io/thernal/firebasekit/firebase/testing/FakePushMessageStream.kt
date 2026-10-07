package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessageStream
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

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
