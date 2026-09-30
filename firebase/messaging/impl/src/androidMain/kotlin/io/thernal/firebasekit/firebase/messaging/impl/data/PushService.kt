package io.thernal.firebasekit.firebase.messaging.impl.data

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.thernal.firebasekit.firebase.bridge.PushHub
import io.thernal.firebasekit.firebase.messaging.api.data.pushMessage

/**
 * Declared in this module's manifest, so it merges into the app. Firebase shows a notification
 * message by itself while the app is in the background; this service sees every data message, and
 * notification messages while the app runs. It hands each to [PushHub] and, for a message with
 * something to show, posts the notification the system would not.
 */
class PushService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val message = pushMessage(
            title = remoteMessage.notification?.title ?: remoteMessage.data["title"],
            body = remoteMessage.notification?.body ?: remoteMessage.data["body"],
            data = remoteMessage.data,
            link = remoteMessage.notification?.link?.toString(),
        )
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (isForeground) {
            PushHub.emitReceived(message)
        }
        // In the foreground the app decides from `messages` whether to show it; the settings say.
        if (!isForeground || PushNotifications.showsInForeground(applicationContext)) {
            PushNotifications.post(context = applicationContext, message = message)
        }
    }

    override fun onNewToken(token: String) {
        PushHub.refreshToken(token)
    }
}
