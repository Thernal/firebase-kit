package io.thernal.firebasekit.firebase.messaging.impl.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.thernal.firebasekit.firebase.messaging.api.data.PushMessage

/**
 * Posts a push message as a notification, configured by the same manifest `<meta-data>` Firebase
 * reads for the notifications it shows itself — so both look the same:
 * `com.google.firebase.messaging.default_notification_channel_id`, `…default_notification_icon`,
 * `…default_notification_color`. One more key is the kit's: [SHOW_IN_FOREGROUND_KEY].
 */
internal object PushNotifications {
    private const val CHANNEL_KEY = "com.google.firebase.messaging.default_notification_channel_id"
    private const val ICON_KEY = "com.google.firebase.messaging.default_notification_icon"
    private const val COLOR_KEY = "com.google.firebase.messaging.default_notification_color"
    private const val SHOW_IN_FOREGROUND_KEY = "push_notifications_show_in_foreground"
    private const val DEFAULT_CHANNEL = "default"
    private const val DEFAULT_CHANNEL_NAME = "Notifications"

    fun showsInForeground(context: Context): Boolean {
        return context.metaData()?.getBoolean(SHOW_IN_FOREGROUND_KEY, true) ?: true
    }

    fun post(
        context: Context,
        message: PushMessage,
    ) {
        if (message.title == null && message.body == null) {
            return
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return
        }
        val metaData = context.metaData()
        val channel = metaData?.getString(CHANNEL_KEY) ?: DEFAULT_CHANNEL
        ensureChannel(context = context, id = channel)
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(metaData?.getInt(ICON_KEY)?.takeIf { it != 0 } ?: context.applicationInfo.icon)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setAutoCancel(true)
            .setContentIntent(openIntent(context = context, message = message))
        metaData?.getInt(COLOR_KEY)?.takeIf { it != 0 }?.let { builder.setColor(context.getColor(it)) }
        try {
            manager.notify(message.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS was revoked between the check and the post.
        }
    }

    /** Creates the channel only when the app has not: an app that wants a translated name creates it first. */
    private fun ensureChannel(
        context: Context,
        id: String,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(id) == null) {
            manager.createNotificationChannel(
                NotificationChannel(id, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    /** The launcher, carrying the message — what a Firebase-shown notification's tap carries too. */
    private fun openIntent(
        context: Context,
        message: PushMessage,
    ): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        message.deepLink?.let { intent.data = Uri.parse(it) }
        message.data.forEach { (key, value) -> intent.putExtra(key, value) }
        message.title?.let { intent.putExtra(PushIntents.TITLE_EXTRA, it) }
        message.body?.let { intent.putExtra(PushIntents.BODY_EXTRA, it) }
        intent.putExtra(PushIntents.MARKER_EXTRA, true)
        return PendingIntent.getActivity(
            context,
            message.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun Context.metaData(): Bundle? {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
    }
}
