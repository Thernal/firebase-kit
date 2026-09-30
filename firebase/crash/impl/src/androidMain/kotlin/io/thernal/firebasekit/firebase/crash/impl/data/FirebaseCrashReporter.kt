package io.thernal.firebasekit.firebase.crash.impl.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter

actual fun platformCrashReporter(): CrashReporter {
    return FirebaseCrashReporter()
}

/**
 * The Crashlytics SDK. Without Firebase configured (no google-services.json) or without the
 * Crashlytics Gradle plugin, `getInstance()` throws; every call is then a no-op.
 */
internal class FirebaseCrashReporter : CrashReporter {
    private val crashlytics: FirebaseCrashlytics? by lazy {
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics?.isCrashlyticsCollectionEnabled = enabled
    }

    override fun setUserId(userId: String?) {
        crashlytics?.setUserId(userId.orEmpty())
    }

    override fun log(message: String) {
        crashlytics?.log(message)
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        crashlytics?.setCustomKey(key, value)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics?.recordException(throwable)
    }
}
