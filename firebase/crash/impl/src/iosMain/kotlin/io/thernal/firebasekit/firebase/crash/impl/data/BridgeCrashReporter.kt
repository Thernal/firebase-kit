package io.thernal.firebasekit.firebase.crash.impl.data

import io.thernal.firebasekit.firebase.bridge.CrashlyticsBridge
import io.thernal.firebasekit.firebase.bridge.FirebaseBridges
import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter

actual fun platformCrashReporter(): CrashReporter {
    return BridgeCrashReporter { FirebaseBridges.crashlytics }
}

/** Crashlytics through the app's Swift bridge; a no-op until the bridge is registered. */
internal class BridgeCrashReporter(
    private val bridge: () -> CrashlyticsBridge?,
) : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) {
        bridge()?.setCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        bridge()?.setUserId(userId.orEmpty())
    }

    override fun log(message: String) {
        bridge()?.log(message)
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        bridge()?.setCustomKey(key = key, value = value)
    }

    override fun recordException(throwable: Throwable) {
        bridge()?.recordError(
            type = throwable::class.simpleName ?: "Throwable",
            message = throwable.message.orEmpty(),
            stackTrace = throwable.stackTraceToString(),
        )
    }
}
