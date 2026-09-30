# firebase — design

Why the kit has the shape it has. It combines Act2Act's `core/firebase` (Compose Multiplatform, Swift
bridges) with ArenaGo's `core/firebase` and `core/remoteconfig` (Android, the better-shaped APIs), with
the choices recorded as epic decisions D56–D60.

## Four capabilities, taken one by one

Crash reporting, remote config, push and sign-in each have their own `api/impl/wiring` and their own
Firebase SDK (D56). An app that only needs crash reports declares one `wiring` and links one SDK;
nothing else reaches its graph or its APK. Act2Act had all four in one module; ArenaGo had no sign-in —
it is here because the owner chose to include it (D56).

## iOS through Swift bridges

Kotlin/Native cannot call the Firebase iOS SDK without linking Firebase's frameworks into the Kotlin
build, the usual source of pain in KMP Firebase setups. Instead (Act2Act's approach, D57) `firebase/bridge`
declares small Kotlin interfaces — `CrashlyticsBridge`, `RemoteConfigBridge`, `MessagingBridge`,
`AuthBridge` — that Swift implements over the Firebase iOS SDK, which stays an ordinary Swift package in
the Xcode project. `FirebaseKit.configure` registers them at launch. A bridge not registered makes its
capability a no-op, the same as Android without `google-services.json`.

The Swift files are part of the kit: `kit.yml`'s `files` section installs them into the app's Xcode folder
and renames their `import` to the app's framework (`framework:`), and `kit update` merges them three ways
like the Kotlin — the reason skill-manager learned `files` (D57). The sample compiles them against the
Kotlin framework and the real SDK, so a Kotlin interface change that the Swift no longer satisfies fails
there.

## Remote config: typed keys, a replaceable source

ArenaGo's shape (D58): a key is `RemoteConfigKey<T>` with its default, so a feature declares its flags as
its own enum (the storage-kit rule: enums fit without extra code) instead of adding to a central list —
Act2Act's single `RemoteConfigKey` enum made every flag a change to core. Reads resolve activated → default
with no registration step.

The transport is a `RemoteConfigSource`: Firebase normally, fixed values for tests and the `regress`
flavor (D28 — regression runs must not depend on a Firebase project's state), or an app's own endpoint.
The source also hands over what was activated on an earlier launch, which Firebase keeps — ArenaGo's
source had nothing to keep, and started from defaults every time.

## Push: a hub outside the graph

The Android `FirebaseMessagingService` and the iOS notification delegate are created by the system, not by
Metro. Both deliver into `PushHub` (in `firebase/bridge`, which Swift can reach), and `messaging/impl`
reads from it. `opened` is a channel: a tap waits until something collects it, so a cold start from a
notification — Act2Act kept a "pending" field in Swift for this — is not lost.

The Android service posts a notification only for messages the system would not show itself, configured
by Firebase's own manifest keys so the two kinds look alike; the tap carries the payload back to the
launcher, where `PushIntents` turns either kind into an `opened` message. ArenaGo posted from the service
in every case and routed taps as intent data; the kit keeps that, adding the foreground choice.

D59 planned a `PushTokenRegistrar` the kit would call; a kit has no app-startup hook to call it from, so it
became `PushMessaging.tokens` — the current token and every new one, for the app to collect in its own
scope.

## Sign-in returns a token, holds no session

`SocialSignIn` ends with a Firebase ID token for the backend to verify — the session is the backend's (and
network-kit's). On Android the Google sheet (Credential Manager) and Apple's web flow need an activity;
Act2Act's app created its bridge in `MainActivity` and handed it over. The kit instead tracks the resumed
activity from app start with androidx.startup, so there is nothing to hand over.

## Crash reporting and App Distribution

`CrashReporter` is the same five calls in both apps (D60). Every flavor reports, told apart by a custom key
the app sets. App Distribution came from build-kit (D27): its lanes live in `fastlane/FirebaseFastfile`,
imported by build-kit's Fastfile, reading the same flavor and app lists, and building through the same
`make` targets.

## What changed from the sources

- One module (Act2Act) → four capabilities; ArenaGo's remote config and push API shapes.
- Act2Act's central `RemoteConfigKey` enum and typed getters → `RemoteConfigKey<T>` / `ConfigKey<T>`.
- Act2Act's listener (`onMessageReceived`, `onNotificationTapped`, `onTokenRefreshed`) → flows.
- Act2Act's `FirebaseAuthBridge` on Android (in the app) → the kit's own Android implementation.
- `FirebaseConfig.initialize()` → Firebase configures itself on Android; `FirebaseKit.configure` on iOS.
- Kotlin exceptions reach Crashlytics on iOS with their class as the issue and the Kotlin stack trace as
  log lines.
