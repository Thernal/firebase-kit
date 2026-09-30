package io.thernal.firebasekit.firebase.messaging.impl.data

import io.thernal.firebasekit.firebase.bridge.FirebaseBridges
import io.thernal.firebasekit.firebase.bridge.MessagingBridge
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal actual fun platformPushPlatform(): PushPlatform {
    return BridgePushPlatform { FirebaseBridges.messaging }
}

internal class BridgePushPlatform(
    private val bridge: () -> MessagingBridge?,
) : PushPlatform {
    override suspend fun token(): String {
        val messaging = messaging()
        return suspendCoroutine { continuation ->
            messaging.token { token, error ->
                if (token != null) {
                    continuation.resume(token)
                } else {
                    continuation.resumeWithException(IllegalStateException(error ?: "No push token"))
                }
            }
        }
    }

    override suspend fun subscribe(topic: String) {
        val messaging = messaging()
        suspendCoroutine { continuation ->
            messaging.subscribe(topic = topic) { error -> continuation.completeWith(error) }
        }
    }

    override suspend fun unsubscribe(topic: String) {
        val messaging = messaging()
        suspendCoroutine { continuation ->
            messaging.unsubscribe(topic = topic) { error -> continuation.completeWith(error) }
        }
    }

    private fun messaging(): MessagingBridge {
        return bridge() ?: error("The messaging bridge is not registered")
    }
}

private fun kotlin.coroutines.Continuation<Unit>.completeWith(error: String?) {
    if (error == null) {
        resume(Unit)
    } else {
        resumeWithException(IllegalStateException(error))
    }
}
