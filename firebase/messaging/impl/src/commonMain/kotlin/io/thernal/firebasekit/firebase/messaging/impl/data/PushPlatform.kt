package io.thernal.firebasekit.firebase.messaging.impl.data

import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging

/** The platform half of [PushMessaging]: the SDK on Android, the Swift bridge on iOS. */
internal interface PushPlatform {
    suspend fun token(): String

    suspend fun subscribe(topic: String)

    suspend fun unsubscribe(topic: String)
}

internal expect fun platformPushPlatform(): PushPlatform
