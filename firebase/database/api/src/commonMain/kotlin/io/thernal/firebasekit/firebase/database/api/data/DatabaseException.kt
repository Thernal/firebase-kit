package io.thernal.firebasekit.firebase.database.api.data

/**
 * A read or write the database refused or could not do. [isPermissionDenied] when its security rules refused
 * it — the one failure an app usually branches on (a node that is gone, or not the user's).
 */
class DatabaseException(
    message: String,
    val isPermissionDenied: Boolean,
    cause: Throwable? = null,
) : Exception(message, cause)
