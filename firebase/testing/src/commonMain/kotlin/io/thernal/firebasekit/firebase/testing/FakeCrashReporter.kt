package io.thernal.firebasekit.firebase.testing

import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter

/** Records what a test's code reported. */
class FakeCrashReporter : CrashReporter {
    val logs = mutableListOf<String>()
    val exceptions = mutableListOf<Throwable>()
    val customKeys = mutableMapOf<String, String>()
    var userId: String? = null
        private set
    var isCollectionEnabled: Boolean = true
        private set

    override fun setCollectionEnabled(enabled: Boolean) {
        isCollectionEnabled = enabled
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun log(message: String) {
        logs += message
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        customKeys[key] = value
    }

    override fun recordException(throwable: Throwable) {
        exceptions += throwable
    }
}
