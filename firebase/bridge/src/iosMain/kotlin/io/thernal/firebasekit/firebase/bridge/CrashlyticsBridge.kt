package io.thernal.firebasekit.firebase.bridge

interface CrashlyticsBridge {
    fun setCollectionEnabled(enabled: Boolean)

    fun setUserId(userId: String)

    fun log(message: String)

    fun setCustomKey(
        key: String,
        value: String,
    )

    fun recordError(
        type: String,
        message: String,
        stackTrace: String,
    )
}
