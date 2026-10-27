# firebase/database/api

The Realtime Database — `RealtimeDatabase`: JSON read and written at a path, with the signed-in user's rights,
task by task. Setup — Gradle, the iOS framework, Xcode and the app delegate — is in
[`firebase/README.md`](../../README.md); why it has this shape, in [`DESIGN.md`](../../DESIGN.md).

## Usage

Values are `kotlinx.serialization` `JsonElement`s; a feature encodes and decodes its own `@Serializable` types
and keeps its own paths.

```kotlin
val json = Json { ignoreUnknownKeys = true }

suspend fun saveProfile(userId: String, profile: ProfileDto) {
    database.set(path = "users/$userId/profile", value = json.encodeToJsonElement(profile))
}

fun profile(userId: String): Flow<ProfileDto?> {
    return database.observe("users/$userId/profile")
        .map { value -> value?.let { json.decodeFromJsonElement<ProfileDto>(it) } }
}
```

| Task | Call |
|---|---|
| Replace a node (`JsonNull` removes it) | `set(path, value)` |
| Several children at once, all or none | `update(path, mapOf("a/b" to …, "c" to JsonNull))` |
| Remove a node | `remove(path)` |
| Read once (the server's value; the cache when offline) | `get(path)` — `null` when there is none |
| Read now and on every change | `observe(path)` — a cold `Flow`; the listener is detached when collection ends |
| A new child's key, ordered by time | `newKey()` — made on the device, no round trip |
| The server's time, an atomic counter | `ServerValue.Timestamp`, `ServerValue.increment(delta)` anywhere inside a written value |

**Failures.** Every call throws `DatabaseException`; `isPermissionDenied` when the security rules refused it —
a node that does not exist any more and is no longer readable reads this way too. An observation ends with
the exception when its read is refused, then or later (a rule that changes, a node deleted under it).

**Offline.** A write completes when the server has it. Offline, the SDK keeps it and sends it when the
connection is back, so the call waits; a screen that has to answer the user bounds it
(`withTimeoutOrNull`). `get` answers from the cache when the server does not.

The default database of the app's Firebase configuration is used — `firebase_url` in `google-services.json`,
`DATABASE_URL` in `GoogleService-Info.plist`. Queries (ordering, limits), transactions and `onDisconnect` are
not in the kit yet: [`docs/todos/database-queries.md`](../../../docs/todos/database-queries.md).

## Tests

`firebase/database/testing` has an in-memory database:

```kotlin
val database = FakeRealtimeDatabase(now = { 1_700_000_000_000 })
database.deny("admin")                       // the rules refuse admin and below
repository.save(…)
assertEquals(expected, database.get("users/ada/profile"))
```

Writes apply at once, observers see every change, `ServerValue`s resolve with `now`, and a node left with no
children disappears, as in Firebase.
