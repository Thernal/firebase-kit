package io.thernal.firebasekit.firebase.config.api.domain

/** A one-off or mixed-type key, for a caller with no enum to put it on. */
data class ConfigKey<T : Any>(
    override val key: String,
    override val default: T,
) : RemoteConfigKey<T>
