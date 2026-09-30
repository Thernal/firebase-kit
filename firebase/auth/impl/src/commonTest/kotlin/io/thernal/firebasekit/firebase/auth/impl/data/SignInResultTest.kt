package io.thernal.firebasekit.firebase.auth.impl.data

import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInResultTest {
    @Test
    fun `a cancel wins over anything else the bridge says`() {
        assertEquals(SignInResult.Cancelled, signInResult(token = "t", isCancelled = true, error = "x"))
    }

    @Test
    fun `a token is a success`() {
        assertEquals(
            SignInResult.Success(idToken = "t", userId = ""),
            signInResult(token = "t", isCancelled = false, error = null),
        )
    }

    @Test
    fun `no token is a failure with the bridge's message`() {
        assertEquals(SignInResult.Failed("denied"), signInResult(token = null, isCancelled = false, error = "denied"))
        assertEquals(
            SignInResult.Failed("Sign-in failed"),
            signInResult(token = null, isCancelled = false, error = null),
        )
    }
}
