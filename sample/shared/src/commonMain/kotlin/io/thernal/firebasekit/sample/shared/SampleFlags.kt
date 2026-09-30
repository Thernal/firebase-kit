package io.thernal.firebasekit.sample.shared

import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigKey

/** The sample's flags: a feature's own enum of remote keys. */
enum class SampleFlags(
    override val key: String,
    override val default: String,
) : RemoteConfigKey<String> {
    GREETING("sample_greeting", default = "Hello from the compiled default"),
}
