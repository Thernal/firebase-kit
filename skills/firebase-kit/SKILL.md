---
name: firebase-kit
description: Writes, reviews and debugs Firebase code in Compose Multiplatform apps that use firebase-kit (packages io.thernal.firebasekit.firebase.*; CrashReporter, RemoteConfig, RemoteConfigKey, ConfigKey, RemoteConfigSource, StaticRemoteConfigSource, PushMessaging, PushMessageStream, PushMessage, PushIntents, SocialSignIn, SignInResult, FirebaseBridges, PushHub, FirebaseKit.configure). Use it for any Firebase work in such a project, even when the kit is not named - crash reports and non-fatals, feature flags and remote config, push notifications, the push token and backend registration, notification taps and deep links, Google or Apple sign-in, Firebase setup on iOS (Swift bridges, app delegate, SPM) or Android (google-services), and Firebase App Distribution. Not for projects without firebase-kit.
---

# firebase-kit

firebase-kit brings Crashlytics, Remote Config, Cloud Messaging and Firebase Authentication (Google, Apple)
to Compose Multiplatform: Android through the SDKs, iOS through Swift bridges in the app's Xcode project.
Guide: https://github.com/Thernal/firebase-kit — `firebase/README.md` (setup, each capability),
`firebase/DESIGN.md` (why).

## 1. Orient first

```sh
grep -rn --include=*.kt -e "CrashWiring\|ConfigWiring\|MessagingWiring\|AuthWiring" . | head   # which capabilities are wired
grep -rn --include=*.kt -e "RemoteConfigKey<" . | head          # existing flag enums — copy their shape
grep -rn --include=*.swift -e "FirebaseKit\." . | head          # the app delegate calls
grep -rn --include=*.kt -e "PushIntents.handle" -e "\.opened" . | head
grep -rn -e "export(projects" --include=*.kts . | grep -i firebase
```

Nothing installed → [references/setup.md](references/setup.md). **Taken as a kit?** A `kits.lock` naming
`firebase-kit` means the modules and the Swift bridges were copied renamed with skill-manager — this skill
too. `skillctl.sh kit status firebase-kit` shows what moved upstream; offer `kit update firebase-kit`
rather than hand edits — it merges the Swift files as well.

## 2. The model

- Four capabilities, each wired by its own container: `CrashReporter`, `RemoteConfig`,
  `PushMessaging` + `PushMessageStream`, `SocialSignIn`. Features inject the `api` types.
- Flags are a feature's own enum implementing `RemoteConfigKey<T>`; `get`, `observe`, `fetchAndActivate`.
- Push: collect `tokens` for the backend; `messages` in the foreground; `opened` (taps) once at the root.
- Sign-in returns a Firebase ID token; the backend's session is the app's.
- iOS: `FirebaseKit.configure(...)` at launch registers the Swift bridges; a missing bridge is a no-op.
- Without Firebase configured everything degrades to defaults and no-ops — nothing crashes.

## 3. Tasks

| Task | Read |
|---|---|
| install, Gradle plugins, iOS export and Xcode, the app delegate | [setup.md](references/setup.md) |
| crash reports, flags, push token and taps, sign-in, tests | [capabilities.md](references/capabilities.md) |
| send a build to testers | [distribution.md](references/distribution.md) |

## 4. Rules

- Never import a Firebase SDK class in a feature — inject the kit's `api` types.
- Never add a flag to a shared list: the feature declares its own `RemoteConfigKey` enum.
- Record only unexpected errors with `recordException`; typed domain failures are outcomes.
- Send every token from `tokens` to the backend, not just the first.
- Collect `opened` exactly once, at the app's root, and route by `deepLink`.
- Replace a source or a capability with `@ContributesTo(AppScope::class, replaces = [...])`, never by
  editing the copied `wiring`.
- Edit the Swift bridges only where the app must; `kit update` merges, but every local edit is a merge.

## 5. Verify

```sh
./gradlew build
```

For the Swift side, build the app's Xcode project.
