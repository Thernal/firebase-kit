package io.thernal.firebasekit.firebase.config.impl.data

import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource

/**
 * Fixed values, as if the server had sent them: tests, previews, and the `regress` flavor, whose runs
 * must not depend on what a Firebase project holds today.
 */
class StaticRemoteConfigSource(
    private val values: Map<String, String> = emptyMap(),
) : RemoteConfigSource {
    override fun activated(): Map<String, String> {
        return values
    }

    override suspend fun fetch(): Map<String, String> {
        return values
    }
}
