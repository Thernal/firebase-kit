package io.thernal.firebasekit.firebase.auth.api.data

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
