package io.thernal.firebasekit.firebase.auth.impl.data

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * The activity on screen, for the sign-in sheets (Credential Manager and the Apple web flow need
 * one). Tracked from app start by androidx.startup, so the app passes nothing in.
 */
internal object ResumedActivity : Application.ActivityLifecycleCallbacks {
    private var current = WeakReference<Activity>(null)

    val activity: Activity?
        get() = current.get()

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (current.get() === activity) {
            current = WeakReference(null)
        }
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        // Only resume and pause matter.
    }

    override fun onActivityStarted(activity: Activity) {
        // Only resume and pause matter.
    }

    override fun onActivityStopped(activity: Activity) {
        // Only resume and pause matter.
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
        // Only resume and pause matter.
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Only resume and pause matter.
    }
}
