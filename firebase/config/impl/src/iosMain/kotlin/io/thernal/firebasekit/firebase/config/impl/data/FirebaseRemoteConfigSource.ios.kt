package io.thernal.firebasekit.firebase.config.impl.data

import io.thernal.firebasekit.firebase.bridge.FirebaseBridges
import io.thernal.firebasekit.firebase.bridge.RemoteConfigBridge
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

actual fun firebaseRemoteConfigSource(
    minimumFetchInterval: Duration,
    fetchTimeout: Duration,
): RemoteConfigSource {
    return BridgeConfigSource(
        minimumFetchInterval = minimumFetchInterval,
        fetchTimeout = fetchTimeout,
        bridge = { FirebaseBridges.remoteConfig },
    )
}

/** Remote Config through the app's Swift bridge; nothing activated and every fetch failing until it is registered. */
internal class BridgeConfigSource(
    private val minimumFetchInterval: Duration,
    private val fetchTimeout: Duration,
    private val bridge: () -> RemoteConfigBridge?,
) : RemoteConfigSource {
    private var isConfigured = false

    override fun activated(): Map<String, String> {
        return bridge()?.activatedValues().orEmpty()
    }

    override suspend fun fetch(): Map<String, String> {
        val remoteConfig = bridge() ?: error("The Remote Config bridge is not registered")
        if (!isConfigured) {
            remoteConfig.configure(
                minimumFetchIntervalSeconds = minimumFetchInterval.inWholeSeconds,
                fetchTimeoutSeconds = fetchTimeout.inWholeSeconds,
            )
            isConfigured = true
        }
        return suspendCoroutine { continuation ->
            remoteConfig.fetchAndActivate { values, error ->
                if (values != null) {
                    continuation.resume(values)
                } else {
                    continuation.resumeWithException(IllegalStateException(error ?: "Remote Config fetch failed"))
                }
            }
        }
    }
}
