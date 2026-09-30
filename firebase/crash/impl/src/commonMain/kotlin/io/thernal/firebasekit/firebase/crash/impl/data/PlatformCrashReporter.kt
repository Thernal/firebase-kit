package io.thernal.firebasekit.firebase.crash.impl.data

import io.thernal.firebasekit.firebase.crash.api.data.CrashReporter

/** Crashlytics for the platform this is compiled for; `wiring` binds it. */
expect fun platformCrashReporter(): CrashReporter
