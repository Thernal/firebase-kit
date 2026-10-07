# firebase/config/api

Remote config — `RemoteConfig`, typed keys, a replaceable source, task by task. Setup — Gradle, the iOS
framework, Xcode and the app delegate — is in [`firebase/README.md`](../../README.md); why it has this
shape, in [`DESIGN.md`](../../DESIGN.md).

## Usage

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
fetch keeps what was active. Fetches are throttled to one an hour (`ConfigProvidersModule`), Firebase's own quota
too (`ConfigSourceProvidersModule`).

**The `regress` flavor, tests, another backend** replace the source, not the config:

```kotlin
@BindingContainer
@ContributesTo(AppScope::class, replaces = [ConfigSourceProvidersModule::class])
interface RegressConfigSource {
    companion object {
        @Provides fun source(): RemoteConfigSource = StaticRemoteConfigSource(mapOf("show_promo_banner" to "true"))
    }
}
```

## Tests

`firebase/testing` has the fake:

```kotlin
val config = testRemoteConfig("checkout_max_items" to "5")
```
