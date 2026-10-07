package io.thernal.firebasekit.firebase.config.api.domain

/** Where values come from — Firebase, a fixed map for tests and regression runs, a backend endpoint. */
interface RemoteConfigSource {
    /** Values activated before — on an earlier launch — read without the network. */
    fun activated(): Map<String, String>

    /** Fetches, activates and returns the new values. Throws on failure. */
    suspend fun fetch(): Map<String, String>
}
