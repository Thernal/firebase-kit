package io.thernal.firebasekit.firebase.database.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.thernal.firebasekit.firebase.database.api.data.RealtimeDatabase
import io.thernal.firebasekit.firebase.database.impl.data.platformRealtimeDatabase

/** Binds [RealtimeDatabase] to the Firebase Realtime Database. */
@BindingContainer
@ContributesTo(AppScope::class)
interface DatabaseProvidersModule {
    companion object {
        @Provides
        fun provideRealtimeDatabase(): RealtimeDatabase {
            return platformRealtimeDatabase()
        }
    }
}
