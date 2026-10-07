# firebase

Setup, then a guide per capability in its `api` module. Why it has this shape: [`DESIGN.md`](DESIGN.md).

## Setup

### Gradle (Android)

In each Android application module (build-kit's `apps/<name>`), the two Firebase Gradle plugins and the
app's own `google-services.json` per flavor (`src/<flavor>/google-services.json`) — the kit installs
`config/firebase/` with an example of each file and what the kit reads from it:

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
3. Add `GoogleService-Info.plist` per flavor (copied into the bundle by a build phase per configuration);
   `config/firebase/GoogleService-Info.example.plist` shows its shape.
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

## Capabilities

Each capability's usage is in its `api` module:

| Capability | Guide |
|---|---|
| Crash reporting | [`crash/api/README.md`](crash/api/README.md) |
| Remote config | [`config/api/README.md`](config/api/README.md) |
| Push | [`messaging/api/README.md`](messaging/api/README.md) |
| Sign-in | [`auth/api/README.md`](auth/api/README.md) |

## Tests

Every capability has a fake in `firebase/testing`; each guide shows its own.

## App Distribution

Non-production builds to testers, through build-kit's `make` and fastlane:

```sh
scripts/distribute-firebase.sh android customer beta qa-team
scripts/distribute-firebase.sh ios beta
```

The app's Fastfile adds `import "FirebaseFastfile"`, its Pluginfile
`gem "fastlane-plugin-firebase_app_distribution"`. The environment carries
`GOOGLE_APPLICATION_CREDENTIALS` and the app ids — `fastlane/.env.firebase.example` lists them; copy it
to `fastlane/.env.firebase` (keep it out of git) and pass `--env firebase`, or set them in CI. The production
flavor is refused: it goes to the stores.
