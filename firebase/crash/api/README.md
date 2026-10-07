# firebase/crash/api

Crash reporting — `CrashReporter`: breadcrumbs, non-fatals, user id and keys, task by task. Setup — Gradle,
the iOS framework, Xcode and the app delegate — is in [`firebase/README.md`](../../README.md); why it has
this shape, in [`DESIGN.md`](../../DESIGN.md).

## Usage

```kotlin
class Startup(private val crash: CrashReporter) {
    fun run() {
        crash.setCustomKey("flavor", Environment.FLAVOR)   // every flavor reports; the key tells them apart
    }
}
// after sign-in / sign-out
crash.setUserId(user.id)
crash.setUserId(null)
```

`log` leaves breadcrumbs; `recordException` records a non-fatal — the unexpected only: a typed domain
failure (a refused login, a validation error) is an outcome, not a bug. With arch-kit, its failure
reporter forwards in one file:

```kotlin
class CrashFailureReporter(private val crash: CrashReporter) : FailureReporter {
    override fun report(throwable: Throwable) = crash.recordException(throwable)
}
```

Without Firebase configured (no google-services.json, the Crashlytics plugin disabled on CI) every call
does nothing.

## Tests

`firebase/testing` has the fake:

```kotlin
val crash = FakeCrashReporter()
```
