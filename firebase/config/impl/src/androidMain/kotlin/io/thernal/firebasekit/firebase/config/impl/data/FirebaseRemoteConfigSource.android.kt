package io.thernal.firebasekit.firebase.config.impl.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration

actual fun firebaseRemoteConfigSource(
    minimumFetchInterval: Duration,
    fetchTimeout: Duration,
): RemoteConfigSource {
    return AndroidFirebaseConfigSource(minimumFetchInterval = minimumFetchInterval, fetchTimeout = fetchTimeout)
}

/** The Remote Config SDK. Without Firebase configured nothing is activated and every fetch fails. */
internal class AndroidFirebaseConfigSource(
    private val minimumFetchInterval: Duration,
    private val fetchTimeout: Duration,
) : RemoteConfigSource {
    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        runCatching { FirebaseRemoteConfig.getInstance() }.getOrNull()
    }
    private var isConfigured = false

    override fun activated(): Map<String, String> {
        return remoteConfig?.all?.mapValues { it.value.asString() }.orEmpty()
    }

    override suspend fun fetch(): Map<String, String> {
        val config = remoteConfig ?: error("Firebase is not configured")
        if (!isConfigured) {
            val settings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = minimumFetchInterval.inWholeSeconds
                fetchTimeoutInSeconds = fetchTimeout.inWholeSeconds
            }
            config.setConfigSettingsAsync(settings).await()
            isConfigured = true
        }
        config.fetchAndActivate().await()
        return config.all.mapValues { it.value.asString() }
    }
}
