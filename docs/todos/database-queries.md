# The Realtime Database beyond read, write and observe

`RealtimeDatabase` has what every app on the database needs: `set`, `update`, `remove`, `get`, `observe`,
`newKey`, `ServerValue`. Left out until an app needs one — each is a method on the api, its Android call,
a bridge method and its Swift, and the fake:

- **Queries** — `orderByChild`/`orderByKey`/`orderByValue` with `limitToFirst`/`limitToLast`, `startAt`/`endAt`,
  for lists too long to observe whole. Probably a `DatabaseQuery` value passed to `get` and `observe`.
- **Child events** — `childAdded`/`childChanged`/`childRemoved`, so a long list is not re-sent on every change.
- **Transactions** — read-modify-write with retries; `ServerValue.increment` covers counters.
- **`onDisconnect`** — presence.
- **Settings** — disk persistence (`setPersistenceEnabled`, before the first use), a database other than the
  default (a URL), the emulator (`useEmulator`) for instrumented tests.

Decide each when an app asks for it, in the shape that holds for any app.
