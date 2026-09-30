package io.thernal.firebasekit.firebase.messaging.impl.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

internal actual fun platformPushPlatform(): PushPlatform {
    return AndroidPushPlatform
}

/** The Messaging SDK. Without Firebase configured every call throws `IllegalStateException`. */
internal object AndroidPushPlatform : PushPlatform {
    override suspend fun token(): String {
        return FirebaseMessaging.getInstance().token.await()
    }

    override suspend fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
    }

    override suspend fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
    }
}
