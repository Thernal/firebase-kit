# Firebase configuration — examples

The shape of the two files each app needs per flavor, with placeholders. The real ones come from the
Firebase console (Project settings → Your apps → download) and are not secrets in the strict sense, but
they name your project: this repository never commits them (`.gitignore`); an application usually does.

| Example | Real file | Where it goes |
|---|---|---|
| `google-services.example.json` | `google-services.json` | Android: `apps/<app>/src/<flavor>/google-services.json` (one per flavor), or beside the app module's build file for one flavor. One file can list several `client`s — the `.dev` and `.beta` application ids of the same project. |
| `GoogleService-Info.example.plist` | `GoogleService-Info.plist` | iOS: one per flavor (e.g. `iosApp/Configuration/Firebase/GoogleService-Info-Beta.plist`), copied into the bundle as `GoogleService-Info.plist` by a build phase per configuration. |

What the kit reads from them:

- `oauth_client` with `client_type` 3 (Android) becomes the `default_web_client_id` string — Google
  sign-in's server client id. Without it Google sign-in fails with "No default_web_client_id resource".
- `CLIENT_ID` and `REVERSED_CLIENT_ID` (iOS) — Google Sign-In; the reversed id is also the app's URL
  scheme.
- `GCM_SENDER_ID`, `IS_GCM_ENABLED` — push.

To try the sample against a project: copy a real file to `sample/android/google-services.json` (package
`io.thernal.firebasekit.sample.android`) and `sample/ios/Sample/GoogleService-Info.plist` (bundle
`io.thernal.firebasekit.sample`); the sample configures Firebase only when they are present.
