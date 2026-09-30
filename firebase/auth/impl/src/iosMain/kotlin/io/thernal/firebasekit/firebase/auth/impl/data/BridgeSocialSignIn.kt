package io.thernal.firebasekit.firebase.auth.impl.data

import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn
import io.thernal.firebasekit.firebase.bridge.AuthBridge
import io.thernal.firebasekit.firebase.bridge.FirebaseBridges
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual fun platformSocialSignIn(): SocialSignIn {
    return BridgeSocialSignIn { FirebaseBridges.auth }
}

/** Google Sign-In and Sign in with Apple through the app's Swift bridge. */
internal class BridgeSocialSignIn(
    private val bridge: () -> AuthBridge?,
) : SocialSignIn {
    override suspend fun signInWithGoogle(): SignInResult {
        val auth = bridge() ?: return SignInResult.Failed("The auth bridge is not registered")
        return suspendCoroutine { continuation ->
            auth.signInWithGoogle { token, cancelled, error ->
                continuation.resume(signInResult(token = token, isCancelled = cancelled, error = error))
            }
        }
    }

    override suspend fun signInWithApple(): SignInResult {
        val auth = bridge() ?: return SignInResult.Failed("The auth bridge is not registered")
        return suspendCoroutine { continuation ->
            auth.signInWithApple { token, cancelled, error ->
                continuation.resume(signInResult(token = token, isCancelled = cancelled, error = error))
            }
        }
    }

    override suspend fun signOut() {
        bridge()?.signOut()
    }
}
