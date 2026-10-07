package io.thernal.firebasekit.firebase.messaging.impl.data

import io.thernal.firebasekit.firebase.bridge.PushHub
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class PushMessagingImpl(
    private val platform: PushPlatform,
    private val refreshedTokens: Flow<String> = PushHub.tokens,
) : PushMessaging {
    override val tokens: Flow<String> = flow {
        // The current token first, if there is one yet; on iOS it waits for the APNs token and
        // arrives through the refresh stream.
        val current = try {
            platform.token()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (
            // No token yet is not an error here: the refresh stream brings it.
            @Suppress("TooGenericExceptionCaught", "SwallowedException") unavailable: Exception,
        ) {
            null
        }
        if (current != null) {
            emit(current)
        }
        emitAll(refreshedTokens)
    }.distinctUntilChanged()

    override suspend fun token(): String {
        return platform.token()
    }

    override suspend fun subscribe(topic: String) {
        platform.subscribe(topic)
    }

    override suspend fun unsubscribe(topic: String) {
        platform.unsubscribe(topic)
    }
}

/** Messaging for the platform this is compiled for; `wiring` binds it. */
fun platformPushMessaging(): PushMessaging {
    return PushMessagingImpl(platform = platformPushPlatform())
}
