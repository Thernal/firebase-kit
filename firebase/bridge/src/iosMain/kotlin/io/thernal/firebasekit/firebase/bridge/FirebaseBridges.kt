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
 * A bridge left out (`nil`) turns its capability into a no-op on iOS. The Realtime Database registers on its
 * own ([registerDatabase], from `FirebaseKit.configureDatabase()` in `ios/FirebaseDatabase`), so an app that
 * does not use it needs neither the Swift file nor the FirebaseDatabase package; unregistered, every read and
 * write fails.
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
    var database: DatabaseBridge? = null
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

    fun registerDatabase(database: DatabaseBridge?) {
        this.database = database
    }
}
