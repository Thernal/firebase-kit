# firebase

Setup, then each capability task by task. Why it has this shape: [`DESIGN.md`](DESIGN.md).

## Setup

### Gradle (Android)

In each Android application module (build-kit's `apps/<name>`), the two Firebase Gradle plugins and the
app's own `google-services.json` per flavor (`src/<flavor>/google-services.json`):

```kotlin
plugins {
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)   // only with crash reporting
}
```

The google-services plugin also generates the `default_web_client_id` string Google sign-in needs.

### Modules

Declare the `wiring` of each capability the app uses in the module that owns the graph, the `api` in each
feature that uses it, `testing` in tests. A capability left out is simply not declared — its container
never reaches the graph.

| Uses | Declare |
|---|---|
| crash reporting | `firebase/crash/{api,wiring}` |
| remote config | `firebase/config/{api,wiring}` |
| push | `firebase/messaging/{api,wiring}`, and `messaging/impl` in the Android app (for `PushIntents`) |
| sign-in | `firebase/auth/{api,wiring}` |
| any, on iOS | `firebase/bridge`, **exported** into the framework (below) |

### iOS framework

The Swift bridges implement Kotlin interfaces, so the framework exports `firebase/bridge`:

```kotlin
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { export(projects.core.firebase.bridge) } }
    sourceSets.iosMain.dependencies { api(projects.core.firebase.bridge) }
}
```

With build-kit, `api(...)` is refused unless listed: add the module to `app.api.allowed` (D29).

### Xcode (iOS)

1. Add the Swift packages `https://github.com/firebase/firebase-ios-sdk` (products FirebaseCore,
   FirebaseCrashlytics, FirebaseRemoteConfig, FirebaseMessaging, FirebaseAuth — those the app uses) and
   `https://github.com/google/GoogleSignIn-iOS` (for Google sign-in).
2. The kit install placed `FirebaseKit.swift` and the bridges in the folder `--place` named; add that folder
   to the app target. Delete a bridge the app does not use and pass `false` for it in `configure`.
3. Add `GoogleService-Info.plist` per flavor (copied into the bundle by a build phase per configuration).
4. Capabilities: Push Notifications, Background Modes → Remote notifications, Sign in with Apple.
5. For Google sign-in, the plist's `REVERSED_CLIENT_ID` as a URL scheme (Info → URL Types).
6. Crashlytics: the `upload-symbols` run script from the Crashlytics package, for dSYMs.

### The app delegate

```swift
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseKit.configure(application: application, launchOptions: launchOptions)
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        FirebaseKit.didRegisterForRemoteNotifications(deviceToken: deviceToken)
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        FirebaseKit.didReceiveRemoteNotification(userInfo: userInfo)
        completionHandler(.newData)
    }

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        FirebaseKit.open(url: url)   // || the app's own deep links
    }
}
```

A SwiftUI app attaches it with `@UIApplicationDelegateAdaptor(AppDelegate.self)`. `configure` must run
before the Kotlin graph uses any capability.

## Crash reporting

```kotlin
class Startup(private val crash: CrashReporter) {
    fun run() {
        crash.setCustomKey("flavor", Environment.FLAVOR)   // every flavor reports; the key tells them apart
    }
}
// after sign-in / sign-out
crash.setUserId(user.id)
crash.setUserId(null)
```

`log` leaves breadcrumbs; `recordException` records a non-fatal — the unexpected only: a typed domain
failure (a refused login, a validation error) is an outcome, not a bug. With arch-kit, its failure
reporter forwards in one file:

```kotlin
class CrashFailureReporter(private val crash: CrashReporter) : FailureReporter {
    override fun report(throwable: Throwable) = crash.recordException(throwable)
}
```

Without Firebase configured (no google-services.json, the Crashlytics plugin disabled on CI) every call
does nothing.

## Remote config

Each feature declares its keys as its own enum; `ConfigKey` covers a one-off:

```kotlin
enum class CheckoutFlags(override val key: String, override val default: Long) : RemoteConfigKey<Long> {
    MAX_ITEMS("checkout_max_items", default = 20),
}
val MIN_BUILD = ConfigKey(key = "min_required_build", default = 0)

remoteConfig.get(CheckoutFlags.MAX_ITEMS)            // now
remoteConfig.observe(CheckoutFlags.MAX_ITEMS)        // now, then every activation
remoteConfig.fetchAndActivate()                      // at startup and on resume; false when throttled or failed
```

`T` is `String`, `Boolean`, `Int`, `Long` or `Double`. A value resolves as: activated from the server → the
key's default; values activated on an earlier launch are read at start, without the network. A failed
fetch keeps what was active. Fetches are throttled to one an hour (`ConfigWiring`), Firebase's own quota
too (`ConfigSourceWiring`).

**The `regress` flavor, tests, another backend** replace the source, not the config:

```kotlin
@BindingContainer
@ContributesTo(AppScope::class, replaces = [ConfigSourceWiring::class])
interface RegressConfigSource {
    companion object {
        @Provides fun source(): RemoteConfigSource = StaticRemoteConfigSource(mapOf("show_promo_banner" to "true"))
    }
}
```

## Push

### The token

```kotlin
appScope.launch {
    pushMessaging.tokens.collect { token -> api.registerPushToken(token) }   // the current one, then every new one
}
pushMessaging.subscribe("news")
```

Send each token to the backend: tokens change (reinstall, restore, expiry). On iOS the first token arrives
once APNs has registered the device — `tokens` waits for it.

### Messages and taps

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

## Sign-in

```kotlin
when (val result = socialSignIn.signInWithApple()) {
    is SignInResult.Success -> backend.signIn(firebaseIdToken = result.idToken)
    SignInResult.Cancelled -> Unit
    is SignInResult.Failed -> showSignInError()   // result.message is for logs
}
socialSignIn.signOut()
```

The kit returns the Firebase ID token; the backend verifies it and issues the app's own session. Android
shows Credential Manager's Google sheet and Firebase's web flow for Apple; iOS the Google Sign-In sheet and
Sign in with Apple. Enable both providers in the Firebase console; Apple on Android needs the Apple
service id and key there too.

## Tests

```kotlin
val crash = FakeCrashReporter()
val config = testRemoteConfig("checkout_max_items" to "5")
val push = FakePushMessaging(initialToken = "t1")
val messages = FakePushMessageStream().apply { open(pushMessage(title = "Hi", body = null, data = mapOf("deepLink" to "app://inbox"))) }
val signIn = FakeSocialSignIn(answer = { SignInResult.Cancelled })
```

## App Distribution

Non-production builds to testers, through build-kit's `make` and fastlane:

```sh
scripts/distribute-firebase.sh android customer beta qa-team
scripts/distribute-firebase.sh ios beta
```

The app's Fastfile adds `import "FirebaseFastfile"`, its Pluginfile
`gem "fastlane-plugin-firebase_app_distribution"`. The environment carries
`GOOGLE_APPLICATION_CREDENTIALS` and the app ids — `FirebaseFastfile`'s header lists them. The production
flavor is refused: it goes to the stores.
