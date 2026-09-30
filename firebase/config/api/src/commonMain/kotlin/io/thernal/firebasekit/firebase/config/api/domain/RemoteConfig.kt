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

/** Where values come from — Firebase, a fixed map for tests and regression runs, a backend endpoint. */
interface RemoteConfigSource {
    /** Values activated before — on an earlier launch — read without the network. */
    fun activated(): Map<String, String>

    /** Fetches, activates and returns the new values. Throws on failure. */
    suspend fun fetch(): Map<String, String>
}
