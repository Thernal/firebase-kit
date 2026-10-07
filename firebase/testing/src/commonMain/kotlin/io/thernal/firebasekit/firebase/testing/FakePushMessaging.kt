package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

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
