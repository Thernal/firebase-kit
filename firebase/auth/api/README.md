# firebase/auth/api

Social sign-in — `SocialSignIn`: Google and Apple, a Firebase ID token for the backend, task by task. Setup
— Gradle, the iOS framework, Xcode and the app delegate — is in [`firebase/README.md`](../../README.md); why
it has this shape, in [`DESIGN.md`](../../DESIGN.md).

## Usage

```kotlin
when (val result = socialSignIn.signInWithApple()) {
    is SignInResult.Success -> backend.signIn(firebaseIdToken = result.idToken)
    SignInResult.Cancelled -> Unit
    is SignInResult.Failed -> showSignInError()   // result.message is for logs
}
socialSignIn.signOut()
```

The kit returns the Firebase ID token; the backend verifies it and issues the app's own session. Android
shows Credential Manager's Google sheet and Firebase's web flow for Apple; iOS the Google Sign-In sheet and
Sign in with Apple. Enable both providers in the Firebase console; Apple on Android needs the Apple
service id and key there too.

## Tests

`firebase/testing` has the fake:

```kotlin
val signIn = FakeSocialSignIn(answer = { SignInResult.Cancelled })
```
