# App Distribution

```sh
make distribute-android APP=<app> FLAVOR=<flavor> BUILD_TYPE=release TESTER_GROUPS=qa   # build-kit's target
scripts/distribute-firebase.sh android <app> <flavor> [debug|release] [--groups a,b] [--testers x@y] [--notes "…"] [--no-bump] [--no-commit] [--no-build] [--dry-run]
scripts/distribute-firebase.sh ios <flavor> [groups]
```

**Android** goes through the Firebase CLI (`firebase appdistribution:distribute`; `npx` when it is not installed), no
Ruby: the script builds with `make build-android`, checks the APK is signed (`apksigner`), reads the Firebase app
id from the flavor's `google-services.json` by the build's `applicationId` (or `FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR>`)
and sends it. Authentication is `firebase login`, or `GOOGLE_APPLICATION_CREDENTIALS` (service account with App
Distribution Admin). Every build it sends first gets the flavor's `versionCode` raised by one (build-kit's
`scripts/bump-version-code.sh`), so testers' phones tell releases apart, and once the build is sent the version file is
committed alone (`chore(release): <app> <flavor> <version> to Firebase App Distribution`), so the number testers have
is the number on record; `--no-commit` leaves it in the working tree, and a commit the repository refuses (a
commit-msg hook) does too, with a warning. `--no-bump` keeps the number, and `--no-build` and `--dry-run` never bump. `--dry-run` builds and checks and sends
nothing. With neither groups nor testers the build is uploaded
and sent to nobody. A non-production variant is signed with the shared debug keystore, else AGP's debug key
(build-kit), so it installs. The script needs build-kit's `scripts/app-properties.sh`, `bump-version-code.sh` and
`make build-android`.

**iOS** goes through fastlane: the app's `fastlane/Fastfile` (build-kit's) has `import "FirebaseFastfile"`;
`fastlane/Pluginfile` has `gem "fastlane-plugin-firebase_app_distribution"`. The same Fastfile has an Android lane
(`bundle exec fastlane android firebase …`) for a pipeline that already runs fastlane.

- Environment: `GOOGLE_APPLICATION_CREDENTIALS`, `FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR>` (optional on Android),
  `FIREBASE_APP_ID_IOS_<FLAVOR>`, optional `FIREBASE_GROUPS` (the default for `--groups`); for iOS also
  `IOS_BUNDLE_ID_<FLAVOR>`, `APP_STORE_CONNECT_API_KEY_PATH`, match. They can sit in `fastlane/.env.firebase` (git-ignored).
- The production flavor is refused — stores only — unless `gradle.properties` has
  `firebase.distribution.production=true`, for an app that never goes to a store (personal, in-house). A production
  release is signed with the release keystore; without one the APK is unsigned and refused.
- A group must exist in the console before it can be addressed (App Distribution → Testers & Groups).
