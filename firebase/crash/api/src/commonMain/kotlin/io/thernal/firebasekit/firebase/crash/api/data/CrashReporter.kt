package io.thernal.firebasekit.firebase.crash.api.data

/**
 * Crash reporting: breadcrumbs, keys and non-fatal errors attached to the next crash report. Every
 * call is safe before Firebase is configured and in builds without it — it does nothing there.
 */
interface CrashReporter {
    /** Turns collection on or off for this install; it stays as set across launches. */
    fun setCollectionEnabled(enabled: Boolean)

    /** Ties reports to a user; `null` clears it (sign-out). */
    fun setUserId(userId: String?)

    /** A breadcrumb shown with the next report. */
    fun log(message: String)

    /** A key shown with every later report — the flavor, the signed-in role. */
    fun setCustomKey(
        key: String,
        value: String,
    )

    /**
     * A non-fatal error. Record the unexpected — a bug caught before it crashed — never an expected
     * outcome such as a validation error or a refused request.
     */
    fun recordException(throwable: Throwable)
}
