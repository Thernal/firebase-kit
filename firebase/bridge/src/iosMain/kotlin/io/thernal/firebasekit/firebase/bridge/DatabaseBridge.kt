package io.thernal.firebasekit.firebase.bridge

/**
 * The Realtime Database for `database/impl`. Values cross as JSON text — `null` for no value — so the Swift
 * side needs no Kotlin types to read or write them. Every callback runs once, except [observe]'s.
 */
interface DatabaseBridge {
    /** `childByAutoId().key` — not `newKey`, which Objective-C would export as `doNewKey`. */
    fun pushKey(): String

    fun set(
        path: String,
        json: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    )

    /** [json] is an object whose keys are paths relative to [path]. */
    fun update(
        path: String,
        json: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    )

    fun remove(
        path: String,
        onComplete: (error: DatabaseBridgeError?) -> Unit,
    )

    fun get(
        path: String,
        onComplete: (json: String?, error: DatabaseBridgeError?) -> Unit,
    )

    /** Calls [onValue] with the value now and after every change, until cancelled or [onError] is called. */
    fun observe(
        path: String,
        onValue: (json: String?) -> Unit,
        onError: (error: DatabaseBridgeError) -> Unit,
    ): DatabaseObservation
}

/** A listener [DatabaseBridge.observe] attached; [cancel] detaches it. */
interface DatabaseObservation {
    fun cancel()
}

/** Made by Swift from the SDK's error: `isPermissionDenied` when the security rules refused the operation. */
class DatabaseBridgeError(
    val message: String,
    val isPermissionDenied: Boolean,
)
