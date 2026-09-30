package io.thernal.firebasekit.firebase.messaging.impl.data

import android.content.Intent
import io.thernal.firebasekit.firebase.bridge.PushHub
import io.thernal.firebasekit.firebase.messaging.api.data.pushMessage

/**
 * Turns the intent a notification tap starts the app with into an `opened` message. Call it from
 * the activity that hosts the app, for the intent it was created with and every later one:
 *
 * ```
 * override fun onCreate(savedInstanceState: Bundle?) { …; if (savedInstanceState == null) PushIntents.handle(intent) }
 * override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); PushIntents.handle(intent) }
 * ```
 *
 * Both kinds of tap arrive the same way: a notification Firebase showed in the background carries the
 * data payload as extras (and `google.message_id`), one the kit posted carries its marker.
 */
object PushIntents {
    internal const val MARKER_EXTRA = "push_opened"
    internal const val TITLE_EXTRA = "push_title"
    internal const val BODY_EXTRA = "push_body"
    private const val FIREBASE_ID_EXTRA = "google.message_id"
    private val PLATFORM_PREFIXES = listOf("google.", "gcm.", "from", "collapse_key")

    /** Returns whether the intent came from a push notification. */
    fun handle(intent: Intent?): Boolean {
        val extras = intent?.extras ?: return false
        if (!extras.containsKey(MARKER_EXTRA) && !extras.containsKey(FIREBASE_ID_EXTRA)) {
            return false
        }
        val data = extras.keySet()
            .filterNot { key -> key == MARKER_EXTRA || key == TITLE_EXTRA || key == BODY_EXTRA }
            .filterNot { key -> PLATFORM_PREFIXES.any(key::startsWith) }
            .mapNotNull { key -> extras.getString(key)?.let { key to it } }
            .toMap()
        val message = pushMessage(
            title = extras.getString(TITLE_EXTRA),
            body = extras.getString(BODY_EXTRA),
            data = data,
            link = intent.dataString,
        )
        PushHub.emitOpened(message)
        // Consumed: a recreated activity must not deliver the same tap again.
        intent.removeExtra(MARKER_EXTRA)
        intent.removeExtra(FIREBASE_ID_EXTRA)
        return true
    }
}
