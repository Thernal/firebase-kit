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

## For AI agents

An application takes the kit by copy:

```sh
skillctl.sh kit install firebase-kit --package <its package> --module <its module path> --alias <its plugin alias> \
    --framework <its Kotlin framework> --place ios/Firebase=<its Xcode source folder>/Firebase
```

That copies the modules below renamed, the Swift bridges into the Xcode folder with their `import`
renamed to the app's framework, `scripts/distribute-firebase.sh` and `fastlane/FirebaseFastfile`, installs
the `firebase-kit` skill, and records it all in `kits.lock`. `kit.yml` lists what the app provides.

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
| `fastlane/FirebaseFastfile`, `scripts/distribute-firebase.sh` | App Distribution lanes | fastlane, build-kit's `make` |
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

No `google-services.json` or `GoogleService-Info.plist` is committed; without them the sample runs with
Firebase unconfigured and every capability says so. Add your own to try it against a project.
