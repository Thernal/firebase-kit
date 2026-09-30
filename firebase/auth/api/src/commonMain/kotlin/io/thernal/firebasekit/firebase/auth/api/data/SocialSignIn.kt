package io.thernal.firebasekit.firebase.auth.api.data

/**
 * Signs in with Google or Apple through Firebase Authentication and returns the Firebase ID token —
 * what the app's backend verifies to create its own session. The kit holds no session: the backend's
 * tokens are the app's (network-kit's `SessionManager`, say).
 */
interface SocialSignIn {
    suspend fun signInWithGoogle(): SignInResult

    suspend fun signInWithApple(): SignInResult

    /** Signs out of Firebase (and Google, so the next sign-in offers the account picker again). */
    suspend fun signOut()
}

sealed interface SignInResult {
    /** [idToken] is a Firebase ID token for the backend; [userId] the Firebase user id. */
    data class Success(
        val idToken: String,
        val userId: String,
    ) : SignInResult

    /** The user closed the sign-in sheet. Nothing to show. */
    data object Cancelled : SignInResult

    /** Sign-in could not complete; [message] is for logs, not for the user. */
    data class Failed(val message: String) : SignInResult
}
