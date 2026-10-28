# firebase/messaging/api

Push — `PushMessaging` (token, topics) and `PushMessageStream` (messages, taps), task by task. Setup —
Gradle, the iOS framework, Xcode and the app delegate — is in [`firebase/README.md`](../../README.md); why
it has this shape, in [`DESIGN.md`](../../DESIGN.md).

## The token

```kotlin
appScope.launch {
    pushMessaging.tokens.collect { token -> api.registerPushToken(token) }   // the current one, then every new one
}
pushMessaging.subscribe("news")
```

Send each token to the backend: tokens change (reinstall, restore, expiry). On iOS the first token arrives
once APNs has registered the device — `tokens` waits for it.

## Messages and taps

```kotlin
pushMessageStream.messages   // received while the app runs
pushMessageStream.opened     // notifications the user tapped — collect once, at the root
    .collect { message -> message.deepLink?.let(deepLinks::open) }
```

A message's destination is its `deepLink` data key (else the notification's link). `opened` keeps a tap
until it is collected, so the tap that launched the app reaches the first screen.

On Android, pass the activity's intents to the kit:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) { …; if (savedInstanceState == null) PushIntents.handle(intent) }
override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); PushIntents.handle(intent) }
```

and configure the notifications with Firebase's own manifest keys, which the kit reads too:

```xml
<meta-data android:name="com.google.firebase.messaging.default_notification_channel_id" android:value="general" />
<meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/ic_notification" />
<meta-data android:name="com.google.firebase.messaging.default_notification_color" android:resource="@color/brand" />
<!-- false: in the foreground only `messages` sees a message; the app decides whether to show it -->
<meta-data android:name="push_notifications_show_in_foreground" android:value="true" />
```

Create the channel at startup with a translated name; otherwise the kit creates it named "Notifications".
The app asks for `POST_NOTIFICATIONS` itself (permissions-kit: `AppPermission.Notification`).

### Drawing a notification yourself (Android)

A message that needs more than a title and a text — action buttons, a channel of its own, a notification to
remove — is drawn by the app. Install a `PushRenderer` in `Application.onCreate`; the service asks it first for
every message, in the foreground and the background, and posts the messages it returns `false` for as before:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PushRenderer.installed = PushRenderer { context, message ->
            when (message.data["type"]) {
                "invite" -> invites.post(context, message)      // Accept / Decline actions; returns true
                "revoked" -> invites.remove(context, message)   // removes the invite's notification; true
                else -> false
            }
        }
    }
}
```

`render` runs on the service's worker thread and must post before it returns; work that takes longer (a
network call) goes to a `WorkManager` job or `goAsync()` in the action's receiver.

## Tests

`firebase/testing` has the fake:

```kotlin
val push = FakePushMessaging(initialToken = "t1")
val messages = FakePushMessageStream().apply { open(pushMessage(title = "Hi", body = null, data = mapOf("deepLink" to "app://inbox"))) }
```
