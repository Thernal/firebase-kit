package io.thernal.firebasekit.firebase.auth.impl.data

import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn

/** Sign-in for the platform this is compiled for; `wiring` binds it. */
expect fun platformSocialSignIn(): SocialSignIn

/** The bridge's callback as a result; the Firebase user id is inside the token, the bridge omits it. */
internal fun signInResult(
    token: String?,
    isCancelled: Boolean,
    error: String?,
): SignInResult {
    return when {
        isCancelled -> SignInResult.Cancelled
        token != null -> SignInResult.Success(idToken = token, userId = "")
        else -> SignInResult.Failed(error ?: "Sign-in failed")
    }
}
