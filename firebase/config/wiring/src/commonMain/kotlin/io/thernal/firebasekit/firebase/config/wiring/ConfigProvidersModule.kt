package io.thernal.firebasekit.firebase.config.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfig
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import io.thernal.firebasekit.firebase.config.impl.domain.RemoteConfigImpl
import kotlin.time.Duration.Companion.hours

/** Binds [RemoteConfig] over whatever [RemoteConfigSource] the graph has. */
@BindingContainer
@ContributesTo(AppScope::class)
interface ConfigProvidersModule {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideRemoteConfig(source: RemoteConfigSource): RemoteConfig {
            return RemoteConfigImpl(source = source, minimumFetchInterval = 1.hours)
        }
    }
}
