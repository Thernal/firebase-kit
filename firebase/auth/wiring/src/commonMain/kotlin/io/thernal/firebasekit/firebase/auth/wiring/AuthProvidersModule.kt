package io.thernal.firebasekit.firebase.auth.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn
import io.thernal.firebasekit.firebase.auth.impl.data.platformSocialSignIn

/** Binds [SocialSignIn] to Firebase Authentication. */
@BindingContainer
@ContributesTo(AppScope::class)
interface AuthProvidersModule {
    companion object {
        @Provides
        fun provideSocialSignIn(): SocialSignIn {
            return platformSocialSignIn()
        }
    }
}
