package io.thernal.firebasekit.firebase.bridge

/**
 * The Swift side of the kit. The app's Swift implements these protocols over the Firebase iOS SDK
 * (the kit ships the implementations in `ios/Firebase`) and registers them before Kotlin needs
 * them, at launch:
 *
 * ```swift
 * FirebaseApp.configure()
 * FirebaseBridges.shared.register(crashlytics: CrashlyticsBridgeImpl(), remoteConfig: RemoteConfigBridgeImpl(),
 *                                 messaging: MessagingBridgeImpl.shared, auth: AuthBridgeImpl())
 * ```
 *
 * A bridge left out (`nil`) turns its capability into a no-op on iOS.
 */
object FirebaseBridges {
    var crashlytics: CrashlyticsBridge? = null
        private set
    var remoteConfig: RemoteConfigBridge? = null
        private set
    var messaging: MessagingBridge? = null
        private set
    var auth: AuthBridge? = null
        private set

    fun register(
        crashlytics: CrashlyticsBridge?,
        remoteConfig: RemoteConfigBridge?,
        messaging: MessagingBridge?,
        auth: AuthBridge?,
    ) {
        this.crashlytics = crashlytics
        this.remoteConfig = remoteConfig
        this.messaging = messaging
        this.auth = auth
    }
}

interface CrashlyticsBridge {
    fun setCollectionEnabled(enabled: Boolean)

    fun setUserId(userId: String)

    fun log(message: String)

    fun setCustomKey(
        key: String,
        value: String,
    )

    fun recordError(
        type: String,
        message: String,
        stackTrace: String,
    )
}

interface RemoteConfigBridge {
    fun configure(
        minimumFetchIntervalSeconds: Long,
        fetchTimeoutSeconds: Long,
    )

    fun activatedValues(): Map<String, String>

    /** Calls back with the activated values, or with an error message. */
    fun fetchAndActivate(onComplete: (values: Map<String, String>?, error: String?) -> Unit)
}

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

interface AuthBridge {
    /** Calls back with a Firebase ID token, or `cancelled`, or an error message. */
    fun signInWithGoogle(onComplete: (idToken: String?, cancelled: Boolean, error: String?) -> Unit)

    fun signInWithApple(onComplete: (idToken: String?, cancelled: Boolean, error: String?) -> Unit)

    fun signOut()
}
