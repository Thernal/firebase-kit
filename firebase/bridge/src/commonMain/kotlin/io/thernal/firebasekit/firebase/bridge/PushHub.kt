package io.thernal.firebasekit.firebase.bridge

import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage
import io.thernal.firebasekit.firebase.messaging.api.data.pushMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

private const val MESSAGE_BUFFER = 16

/**
 * Where push events enter Kotlin. The Android messaging service and the iOS Swift bridge are created
 * by the system, outside the dependency graph, so they deliver here and `messaging/impl` reads from
 * here. Swift calls [receive], [open] and [refreshToken].
 */
object PushHub {
    private val mutableMessages = MutableSharedFlow<PushMessage>(extraBufferCapacity = MESSAGE_BUFFER)
    private val openedChannel = Channel<PushMessage>(Channel.BUFFERED)
    private val mutableTokens = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1)

    val messages: Flow<PushMessage> = mutableMessages.asSharedFlow()

    /** One collector; a message waits in the channel until it is taken. */
    val opened: Flow<PushMessage> = openedChannel.receiveAsFlow()

    val tokens: Flow<String> = mutableTokens.asSharedFlow()

    fun emitReceived(message: PushMessage) {
        mutableMessages.tryEmit(message)
    }

    fun emitOpened(message: PushMessage) {
        openedChannel.trySend(message)
    }

    /** From Swift: a message received while the app runs. */
    fun receive(
        title: String?,
        body: String?,
        data: Map<String, String>,
    ) {
        emitReceived(pushMessage(title = title, body = body, data = data))
    }

    /** From Swift: a notification the user tapped. */
    fun open(
        title: String?,
        body: String?,
        data: Map<String, String>,
    ) {
        emitOpened(pushMessage(title = title, body = body, data = data))
    }

    /** From the platform: a new registration token. */
    fun refreshToken(token: String) {
        mutableTokens.tryEmit(token)
    }
}
