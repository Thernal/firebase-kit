package io.thernal.firebasekit.firebase.config.api.domain

import kotlinx.coroutines.flow.Flow

/**
 * Typed remote configuration. Every key carries its default, so reads need no registration step.
 * A value resolves as: activated from the server → the key's default. A failed or throttled fetch
 * keeps what was activated before.
 */
interface RemoteConfig {
    fun <T : Any> get(key: RemoteConfigKey<T>): T

    /** The current value, then every change an activation brings. */
    fun <T : Any> observe(key: RemoteConfigKey<T>): Flow<T>

    /** Fetches and activates the latest values; `false` when throttled or failed. */
    suspend fun fetchAndActivate(): Boolean
}
