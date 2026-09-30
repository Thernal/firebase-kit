package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn

/** Sign-in answered by [answer]; [attempts] records which provider each call used. */
class FakeSocialSignIn(
    var answer: (Provider) -> SignInResult = { SignInResult.Success(idToken = "test-id-token", userId = "test-user") },
) : SocialSignIn {
    enum class Provider { Google, Apple }

    val attempts = mutableListOf<Provider>()
    var signOuts: Int = 0
        private set

    override suspend fun signInWithGoogle(): SignInResult {
        attempts += Provider.Google
        return answer(Provider.Google)
    }

    override suspend fun signInWithApple(): SignInResult {
        attempts += Provider.Apple
        return answer(Provider.Apple)
    }

    override suspend fun signOut() {
        signOuts++
    }
}
