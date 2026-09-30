# Run the sample against a Firebase project

**Status:** open

`./gradlew build` builds and lints the Android sample; `xcodebuild` compiles the iOS sample with the kit's
Swift bridges against the Firebase iOS SDK, and it launches on the simulator with Firebase unconfigured
(defaults shown, nothing crashes). Nobody has run it against a real project: put a `google-services.json`
and a `GoogleService-Info.plist` where `config/firebase/README.md` says, then fetch a config value, receive a push in the foreground and the
background, open one from a cold start (Android and iOS), sign in with Google and Apple, and see a
non-fatal in the Crashlytics console with its Kotlin stack trace. Note what was seen here.
