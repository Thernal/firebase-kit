package io.thernal.firebasekit.firebase.crash.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter
import io.thernal.firebasekit.firebase.crash.impl.data.platformCrashReporter

/** Binds [CrashReporter] to Crashlytics. An app without crash reporting leaves this container out. */
@BindingContainer
@ContributesTo(AppScope::class)
interface CrashProvidersModule {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideCrashReporter(): CrashReporter {
            return platformCrashReporter()
        }
    }
}
