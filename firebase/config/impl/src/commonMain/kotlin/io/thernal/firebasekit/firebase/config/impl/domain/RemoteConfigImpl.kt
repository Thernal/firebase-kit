package io.thernal.firebasekit.firebase.config.impl.domain

import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfig
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigKey
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * The value store over a [RemoteConfigSource]: what it activated on an earlier launch at first, the
 * latest after [fetchAndActivate]. Fetches closer together than [minimumFetchInterval] are skipped.
 */
class RemoteConfigImpl(
    private val source: RemoteConfigSource,
    private val minimumFetchInterval: Duration = 1.hours,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : RemoteConfig {
    private val values by lazy { MutableStateFlow(readActivated()) }
    private var lastFetch: TimeMark? = null

    override fun <T : Any> get(key: RemoteConfigKey<T>): T {
        return decodeConfigValue(raw = values.value[key.key], default = key.default)
    }

    override fun <T : Any> observe(key: RemoteConfigKey<T>): Flow<T> {
        return values
            .map { decodeConfigValue(raw = it[key.key], default = key.default) }
            .onStart { emit(get(key)) }
            .distinctUntilChanged()
    }

    override suspend fun fetchAndActivate(): Boolean {
        val previous = lastFetch
        if (previous != null && previous.elapsedNow() < minimumFetchInterval) {
            return false
        }
        val fetched = try {
            source.fetch()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (
            // Whatever the transport throws is a failed fetch: the activated values stay.
            @Suppress("TooGenericExceptionCaught", "SwallowedException") failure: Exception,
        ) {
            return false
        }
        lastFetch = timeSource.markNow()
        values.value = fetched
        return true
    }

    private fun readActivated(): Map<String, String> {
        return runCatching { source.activated() }.getOrDefault(emptyMap())
    }
}
