package io.thernal.firebasekit.firebase.config.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraph
import io.thernal.firebasekit.firebase.config.api.domain.ConfigKey
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfig
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfigSource
import io.thernal.firebasekit.firebase.config.impl.data.StaticRemoteConfigSource
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the `regress` flavor contributes: fixed values in place of Firebase. */
@BindingContainer
@ContributesTo(AppScope::class, replaces = [ConfigSourceWiring::class])
interface RegressConfigSourceWiring {
    companion object {
        @Provides
        fun provideRemoteConfigSource(): RemoteConfigSource {
            return StaticRemoteConfigSource(mapOf("maintenance" to "true"))
        }
    }
}

@DependencyGraph(AppScope::class)
interface TestAppGraph {
    val remoteConfig: RemoteConfig
}

class RegressSourceTest {
    @Test
    fun `a replacing container swaps Firebase for fixed values`() {
        val config = createGraph<TestAppGraph>().remoteConfig

        assertEquals(true, config.get(ConfigKey(key = "maintenance", default = false)))
    }
}
