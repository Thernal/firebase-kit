package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfig
import io.thernal.firebasekit.firebase.config.impl.data.StaticRemoteConfigSource
import io.thernal.firebasekit.firebase.config.impl.domain.RemoteConfigImpl
import kotlin.time.Duration

/**
 * A [RemoteConfig] holding [values] as if the server had sent them — keys absent from it read as
 * their defaults. `testRemoteConfig("show_promo_banner" to "true")`.
 */
fun testRemoteConfig(vararg values: Pair<String, String>): RemoteConfig {
    return RemoteConfigImpl(source = StaticRemoteConfigSource(values.toMap()), minimumFetchInterval = Duration.ZERO)
}
