package io.thernal.firebasekit.firebase.bridge

interface AuthBridge {
    /** Calls back with a Firebase ID token, or `cancelled`, or an error message. */
    fun signInWithGoogle(onComplete: (idToken: String?, cancelled: Boolean, error: String?) -> Unit)

    fun signInWithApple(onComplete: (idToken: String?, cancelled: Boolean, error: String?) -> Unit)

    fun signOut()
}
