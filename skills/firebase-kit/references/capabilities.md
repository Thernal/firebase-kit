# Capabilities

## Crash reporting

```kotlin
crash.setCustomKey("flavor", Environment.FLAVOR)   // at startup
crash.setUserId(user.id)                           // after sign-in; null after sign-out
crash.log("checkout: payment sheet opened")
crash.recordException(unexpected)                  // never a typed domain failure
```

arch-kit: a one-file `FailureReporter` forwarding to `recordException` (`firebase/README.md`).

## Remote config

```kotlin
enum class CheckoutFlags(override val key: String, override val default: Boolean) : RemoteConfigKey<Boolean> {
    APPLE_PAY("checkout_apple_pay", default = false),
}
val enabled: Flow<Boolean> = remoteConfig.observe(CheckoutFlags.APPLE_PAY)
remoteConfig.fetchAndActivate()   // startup, resume; false when throttled (1 h) or failed
```

Types: String, Boolean, Int, Long, Double. An unparsable server value reads as the default. For
`regress` or tests replace `ConfigSourceWiring` with a `StaticRemoteConfigSource`, or use
`testRemoteConfig("key" to "value")`.

## Push

```kotlin
appScope.launch { pushMessaging.tokens.collect(api::registerPushToken) }
pushMessaging.subscribe("news")
LaunchedEffect(Unit) { pushMessageStream.opened.collect { it.deepLink?.let(deepLinks::open) } }   // root only
pushMessageStream.messages.collect { showInAppBanner(it) }   // foreground
```

The destination travels in the `deepLink` data key of the message the backend sends.

## Sign-in

```kotlin
when (val result = socialSignIn.signInWithGoogle()) {
    is SignInResult.Success -> onIntent(Intent.SignedIn(result.idToken))   // exchange at the backend
    SignInResult.Cancelled -> Unit
    is SignInResult.Failed -> onIntent(Intent.SignInFailed)
}
```

Sign-out: `socialSignIn.signOut()` and the app's own session.

## Tests

`FakeCrashReporter` (records), `testRemoteConfig(...)`, `FakePushMessaging(initialToken)` (`newToken`,
`topics`), `FakePushMessageStream` (`receive`, `open`), `FakeSocialSignIn(answer)` (`attempts`, `signOuts`).
