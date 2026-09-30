package io.thernal.firebasekit.sample.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.thernal.firebasekit.firebase.messaging.impl.data.PushIntents
import io.thernal.firebasekit.sample.shared.SampleApp

/**
 * Without a google-services.json (none is committed) Firebase is not configured: every capability
 * answers "not configured" and nothing crashes. Add one and the google-services plugin to try it.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            PushIntents.handle(intent)
        }
        setContent { SampleApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        PushIntents.handle(intent)
    }
}
