package io.thernal.firebasekit.sample.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn
import io.thernal.firebasekit.firebase.config.api.domain.RemoteConfig
import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessageStream
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessaging

/** The graph an application has; here it only exposes what the four wirings bind. */
@DependencyGraph(AppScope::class)
interface SampleGraph {
    val crashReporter: CrashReporter
    val remoteConfig: RemoteConfig
    val pushMessaging: PushMessaging
    val pushMessages: PushMessageStream
    val signIn: SocialSignIn
}
