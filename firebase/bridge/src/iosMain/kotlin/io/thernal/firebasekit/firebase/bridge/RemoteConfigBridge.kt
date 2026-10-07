package io.thernal.firebasekit.firebase.bridge

interface RemoteConfigBridge {
    fun configure(
        minimumFetchIntervalSeconds: Long,
        fetchTimeoutSeconds: Long,
    )

    fun activatedValues(): Map<String, String>

    /** Calls back with the activated values, or with an error message. */
    fun fetchAndActivate(onComplete: (values: Map<String, String>?, error: String?) -> Unit)
}
