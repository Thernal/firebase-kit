# Setup

## Install

```sh
skillctl.sh kit install firebase-kit --package <app package> --module <e.g. :core:firebase> --alias <plugin alias> \
    --framework <the app's Kotlin framework, e.g. ComposeApp> --place ios/Firebase=<Xcode source folder>/Firebase
```

The Kotlin modules land under the module path; the Swift bridges in the Xcode folder with `import` renamed;
`scripts/distribute-firebase.sh` and `fastlane/FirebaseFastfile` at the root. The catalog needs
`firebase-bom`, `firebase-crashlytics`, `firebase-config`, `firebase-messaging`, `firebase-auth`,
`kotlinx-coroutines-play-services`, `androidx-core`, `lifecycle-process`, `androidx-credentials`,
`androidx-credentials-play-services-auth`, `google-identity-googleid`, `androidx-startup`,
`kotlinx-coroutines-core`, `metro-runtime`, and the plugins `google-services`, `firebase-crashlytics`.

## Android

- Apply `google-services` (and `firebase-crashlytics`) to each application module; a
  `google-services.json` per flavor under `src/<flavor>/`.
- Declare each used capability's `wiring` where the graph is, `api` in features.
- Push: `PushIntents.handle(intent)` in the host activity's `onCreate` and `onNewIntent`; the channel and
  icon as Firebase's `default_notification_*` meta-data; ask `POST_NOTIFICATIONS` (permissions-kit).

## iOS

- Export `firebase/bridge` into the framework (`export(...)` + `api(...)` in `iosMain`; with build-kit list
  it in `app.api.allowed`).
- SPM: firebase-ios-sdk (the products used) and GoogleSignIn-iOS 9.x.
- `GoogleService-Info.plist` per flavor; capabilities Push Notifications, Background Modes (remote
  notifications), Sign in with Apple; `REVERSED_CLIENT_ID` URL scheme for Google.
- The app delegate calls `FirebaseKit.configure(application:launchOptions:)`,
  `didRegisterForRemoteNotifications(deviceToken:)`, `didReceiveRemoteNotification(userInfo:)`,
  `open(url:)` — full code in `firebase/README.md` → The app delegate.

## Checks

- iOS: every capability does nothing → `FirebaseKit.configure` not called, or before the Kotlin graph? It
  must run first. A capability passed `false` stays off.
- iOS build: "cannot find type 'CrashlyticsBridge'" → `firebase/bridge` not exported into the framework.
- Android: Google sign-in "No default_web_client_id resource" → the google-services plugin is not applied.
- No token on iOS → push capability, APNs key in the Firebase console, real device (not the simulator).
