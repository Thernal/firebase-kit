package io.thernal.firebasekit.firebase.messaging.api.data

import android.content.Context

/**
 * Draws some messages' notifications on Android itself — with actions, on a channel of its own — or keeps one
 * from showing. The service asks it first for every message it receives, in the foreground and in the
 * background; a message it does not take is posted as before.
 */
fun interface PushRenderer {
    /** `true` when it took [message]: posted a notification for it, or chose to show none. */
    fun render(
        context: Context,
        message: PushMessage,
    ): Boolean

    companion object {
        /**
         * Set in `Application.onCreate`: the process a message wakes runs it before the service, so no message
         * comes before the renderer.
         */
        @Volatile
        var installed: PushRenderer? = null
    }
}
