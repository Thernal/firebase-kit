# Capabilities

## Crash reporting

```kotlin
crash.setCustomKey("flavor", Environment.FLAVOR)   // at startup
crash.setUserId(user.id)                           // after sign-in; null after sign-out
crash.log("checkout: payment sheet opened")
crash.recordException(unexpected)                  // never a typed domain failure
```

arch-kit: a one-file `FailureReporter` forwarding to `recordException` (`firebase/crash/api/README.md`).

## Remote config

```kotlin
enum class CheckoutFlags(override val key: String, override val default: Boolean) : RemoteConfigKey<Boolean> {
    APPLE_PAY("checkout_apple_pay", default = false),
}
val enabled: Flow<Boolean> = remoteConfig.observe(CheckoutFlags.APPLE_PAY)
remoteConfig.fetchAndActivate()   // startup, resume; false when throttled (1 h) or failed
```

Types: String, Boolean, Int, Long, Double. An unparsable server value reads as the default. For
`regress` or tests replace `ConfigSourceProvidersModule` with a `StaticRemoteConfigSource`, or use
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

## Realtime Database

```kotlin
class ProfileRemoteDataSourceImpl(private val database: RealtimeDatabase) : ProfileRemoteDataSource {
    override suspend fun save(userId: String, profile: ProfileDto) {
        database.set(path = "users/$userId/profile", value = Json.encodeToJsonElement(profile))
    }

    override fun profile(userId: String): Flow<ProfileDto?> {
        return database.observe("users/$userId/profile").map { it?.let(Json::decodeFromJsonElement) }
    }
}
```

`update(path, mapOf("a/b" to …, "c" to JsonNull))` writes several children atomically; `ServerValue.Timestamp`
and `ServerValue.increment(n)` resolve on the server; `newKey()` names a new child. Catch `DatabaseException`
— `isPermissionDenied` is a rules refusal, including a node deleted under the reader. Writes wait offline:
`withTimeoutOrNull` where a screen waits. Queries, transactions and `onDisconnect` are not in the kit
(`docs/todos/database-queries.md`).

## Tests

`FakeCrashReporter` (records), `testRemoteConfig(...)`, `FakePushMessaging(initialToken)` (`newToken`,
`topics`), `FakePushMessageStream` (`receive`, `open`), `FakeSocialSignIn(answer)` (`attempts`, `signOuts`);
`FakeRealtimeDatabase(now)` (`deny`, `allow`; an in-memory tree) in `firebase/database/testing`.
