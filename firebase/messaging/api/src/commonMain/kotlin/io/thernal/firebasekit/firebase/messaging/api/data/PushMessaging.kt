package io.thernal.firebasekit.firebase.messaging.api.data

import kotlinx.coroutines.flow.Flow

/** This install's push address and its topics. */
interface PushMessaging {
    /**
     * The registration token now, then each new one. Collect it for the app's lifetime and send each
     * to the backend — tokens change (reinstall, restore, expiry) and a stale one receives nothing.
     */
    val tokens: Flow<String>

    /** The current token. Throws when push is unavailable (Firebase not configured, no APNs token yet). */
    suspend fun token(): String

    suspend fun subscribe(topic: String)

    suspend fun unsubscribe(topic: String)
}
