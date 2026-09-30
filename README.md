# firebase-kit

Firebase in a Compose Multiplatform app (android, iosArm64, iosSimulatorArm64), as four capabilities an
app takes one by one: **crash reporting** (Crashlytics), **remote config** with typed keys a feature owns,
**push** (Cloud Messaging — token, topics, messages and taps), and **sign-in** with Google and Apple
returning a Firebase ID token. Android uses the Firebase SDKs directly; iOS reaches the Firebase iOS SDK
through Swift bridges the kit ships into the app's Xcode project. Firebase App Distribution lanes come
with it.

```kotlin
enum class HomeFlags(override val key: String, override val default: Boolean) : RemoteConfigKey<Boolean> {
    SHOW_PROMO_BANNER("show_promo_banner", default = false),
}

class HomeViewModel(remoteConfig: RemoteConfig, private val crash: CrashReporter) : ViewModel() {
    val showBanner: Flow<Boolean> = remoteConfig.observe(HomeFlags.SHOW_PROMO_BANNER)
}
```

```kotlin
when (val result = socialSignIn.signInWithGoogle()) {
    is SignInResult.Success -> session.signIn(api.exchange(result.idToken))   // the backend's session
    SignInResult.Cancelled -> Unit
    is SignInResult.Failed -> crash.log(result.message)
}
```

## Documentation

| Read | For |
|---|---|
| this file | what is here and how it is built |
| [`firebase/README.md`](firebase/README.md) | setup (Gradle, Xcode, the bridges, the app delegate), then each capability task by task, App Distribution |
| [`firebase/DESIGN.md`](firebase/DESIGN.md) | why each part has its shape, and what changed from the apps it came from |
| [`skills/firebase-kit`](skills/firebase-kit/SKILL.md) | the same for an agent working in an app that uses the kit |

## Installing

An application takes the kit **by copy**, not as a dependency: the code is copied into the app, renamed to the app's own package, and belongs to the app from then on. Nothing is published to a Maven repository.

### With skill-manager

If you have access to the author's knowledge repository (`github.com/Thernal/knowledge`), its **skill-manager** skill does all of it — copy, rename, the skill, and later updates:

```sh
skillctl.sh kit install firebase-kit --package com.example.app --module :core:firebase --alias app \
    --framework ComposeApp --place ios/Firebase=iosApp/iosApp/Firebase
```

It copies the `code` parts of [`kit.yml`](kit.yml) renamed, places its `files` where the app keeps them (`--place`), installs the `firebase-kit` skill and records the copy in `kits.lock`. `kit status` then shows what changed upstream and what the app edited; `kit update` merges the kit's changes three ways, keeping the app's edits. The install prints what the app must provide (`requires`).

### Without it

The same by hand, from a clone of this repository.

1. **Copy** the paths listed under `code` in [`kit.yml`](kit.yml) into the app, under the module path the app gives them: `firebase/…` → `core/firebase/…`; copy the paths under `files` where the app keeps such files (`ios/Firebase`, `scripts/distribute-firebase.sh`, `fastlane/FirebaseFastfile`, `fastlane/.env.firebase.example`, `config/firebase`). Note the commit you copied (`git rev-parse HEAD`) — updates start from it.
2. **Rename** in everything copied:

   | In the kit | Becomes | Where |
   |---|---|---|
   | `io.thernal.firebasekit` | the app's package, e.g. `com.example.app` | sources, build files; and the directories `io/thernal/firebasekit` |
   | `:firebase:` and `":firebase"`, `projects.firebase.` | the module path, e.g. `:core:firebase:`, `projects.core.firebase.` | build files |
   | `libs.plugins.firebasekit.` | the app's catalog alias, e.g. `libs.plugins.app.` | build files |
   | `import SampleShared` | `import` of the app's Kotlin framework | Swift files |

   ```sh
   # in the app, after copying — perl, so it runs the same on macOS and Linux
   grep -rlI -e io.thernal.firebasekit -e io/thernal/firebasekit -e :firebase -e plugins.firebasekit. -e SampleShared core/firebase iosApp/iosApp/Firebase scripts/distribute-firebase.sh fastlane/FirebaseFastfile fastlane/.env.firebase.example config/firebase \
     | xargs perl -pi -e 's/\Qio.thernal.firebasekit\E/com.example.app/g; s{\Qio/thernal/firebasekit\E}{com/example/app}g; s/\Q:firebase:\E/:core:firebase:/g; s/"\Q:firebase\E"/":core:firebase"/g; s/projects\.\Qfirebase\E\./projects.core.firebase./g; s/libs\.plugins\.\Qfirebasekit\E\./libs.plugins.app./g; s/^(\s*(?:@\w+\s+)?)import \QSampleShared\E$/$1import ComposeApp/'
   find core/firebase -depth -type d -path '*/io/thernal/firebasekit' | while read -r d; do
     mkdir -p "${d%/io/thernal/firebasekit}/com/example" && mv "$d" "${d%/io/thernal/firebasekit}/com/example/app"
   done
   find core/firebase -depth -type d -empty -delete
   ```

3. **Provide** what the copy expects — the `requires` list in [`kit.yml`](kit.yml): convention plugins (build-kit's, or the ones in this repository's `build-logic/convention`), catalog entries, settings — and, where listed, platform setup.
4. **The skill** (optional): copy [`skills/firebase-kit`](skills/firebase-kit) into the app's skills directory (`.claude/skills/` for Claude Code), with the same renames, so an agent working in the app knows the kit.
5. **Updates** are yours to carry: `git diff <the commit you copied> <a newer one> -- <the code paths>` in the kit shows what changed; apply what you want, renamed the same way.

## Layout

| Part | Holds | Depends on |
|---|---|---|
| `firebase/crash/{api,impl,wiring}` | `CrashReporter`; Crashlytics | Crashlytics (Android) |
| `firebase/config/{api,impl,wiring}` | `RemoteConfigKey<T>`, `ConfigKey`, `RemoteConfig`, `RemoteConfigSource`; `DefaultRemoteConfig`, `StaticRemoteConfigSource`, the Firebase source | Remote Config (Android) |
| `firebase/messaging/{api,impl,wiring}` | `PushMessage`, `PushMessaging`, `PushMessageStream`; the Android messaging service, notification and `PushIntents` | Messaging, core, lifecycle-process (Android) |
| `firebase/auth/{api,impl,wiring}` | `SocialSignIn`, `SignInResult`; Credential Manager (Google) and Firebase's Apple flow | Auth, Credential Manager, Google ID, startup (Android) |
| `firebase/bridge` | the interfaces the Swift implements, `FirebaseBridges`, `PushHub` — exported into the iOS framework | messaging/api |
| `firebase/testing` | `FakeCrashReporter`, `testRemoteConfig()`, `FakePushMessaging`, `FakePushMessageStream`, `FakeSocialSignIn` | the apis |
| `ios/Firebase` | `FirebaseKit.swift` and one bridge per capability | Firebase iOS SDK, GoogleSignIn (SPM) |
| `fastlane/FirebaseFastfile`, `scripts/distribute-firebase.sh`, `fastlane/.env.firebase.example` | App Distribution lanes and their environment | fastlane, build-kit's `make` |
| `config/firebase` | example `google-services.json` and `GoogleService-Info.plist`, and what the kit reads from them | — |
| `sample/{shared,android,ios}` | one screen using all four | the kit — never copied |

## Building

```sh
./gradlew build
```

Every target, tests on the JVM host and the iOS simulator — remote config (defaults, activation,
throttling, decoding, a failed fetch), the push token stream and taps kept until collected, the iOS
bridges' adapters, sign-in results, the fakes, and a Metro graph that replaces Firebase with fixed values —
Detekt, which fails on any finding, Android lint on the sample, and the sample's iOS framework link. No
Firebase project is needed: the build never talks to Firebase.

The iOS sample compiles the Swift bridges against the Kotlin framework and the Firebase iOS SDK (fetched
by Swift Package Manager on the first build):

```sh
xcodebuild -project sample/ios/Sample.xcodeproj -scheme Sample -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' build
```

No `google-services.json` or `GoogleService-Info.plist` is committed — `config/firebase` has examples of
both. Without them the sample runs with Firebase unconfigured and every capability says so; put real ones
at `sample/android/google-services.json` and `sample/ios/Sample/GoogleService-Info.plist` and it runs
against that project.
