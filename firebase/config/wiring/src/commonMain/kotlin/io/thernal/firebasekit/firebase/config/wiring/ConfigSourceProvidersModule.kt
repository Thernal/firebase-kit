package io.thernal.firebasekit.firebase.config.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import io.thernal.firebasekit.firebase.config.impl.data.firebaseRemoteConfigSource
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Firebase as the source. The `regress` flavor, or an app with its own endpoint, replaces this
 * container: `@ContributesTo(AppScope::class, replaces = [ConfigSourceProvidersModule::class])` providing a
 * `StaticRemoteConfigSource` or its own [RemoteConfigSource].
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface ConfigSourceProvidersModule {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideRemoteConfigSource(): RemoteConfigSource {
            return firebaseRemoteConfigSource(minimumFetchInterval = 1.hours, fetchTimeout = 1.minutes)
        }
    }
}
