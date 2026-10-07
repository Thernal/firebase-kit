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
