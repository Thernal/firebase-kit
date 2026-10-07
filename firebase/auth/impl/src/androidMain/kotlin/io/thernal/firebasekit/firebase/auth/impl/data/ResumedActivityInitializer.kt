package io.thernal.firebasekit.firebase.auth.impl.data

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class ResumedActivityInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(ResumedActivity)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
