package io.thernal.firebasekit.firebase.bridge

interface MessagingBridge {
    fun token(onComplete: (token: String?, error: String?) -> Unit)

    fun subscribe(
        topic: String,
        onComplete: (error: String?) -> Unit,
    )

    fun unsubscribe(
        topic: String,
        onComplete: (error: String?) -> Unit,
    )
}
