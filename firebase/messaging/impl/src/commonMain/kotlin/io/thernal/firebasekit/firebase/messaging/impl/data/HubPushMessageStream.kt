package io.thernal.firebasekit.firebase.messaging.impl.data

import io.thernal.firebasekit.firebase.bridge.PushHub
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessageStream
import kotlinx.coroutines.flow.Flow

internal object HubPushMessageStream : PushMessageStream {
    override val messages: Flow<PushMessage> = PushHub.messages
    override val opened: Flow<PushMessage> = PushHub.opened
}

fun pushMessageStream(): PushMessageStream {
    return HubPushMessageStream
}
