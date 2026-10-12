# App Distribution

```sh
make distribute-android APP=<app> FLAVOR=<flavor> BUILD_TYPE=release TESTER_GROUPS=qa   # build-kit's target
scripts/distribute-firebase.sh android <app> <flavor> [debug|release] [--groups a,b] [--testers x@y] [--notes "…"] [--bump] [--no-build] [--dry-run]
scripts/distribute-firebase.sh ios <flavor> [groups]
```

**Android** goes through the Firebase CLI (`firebase appdistribution:distribute`; `npx` when it is not installed), no
Ruby: the script builds with `make build-android`, checks the APK is signed (`apksigner`), reads the Firebase app
id from the flavor's `google-services.json` by the build's `applicationId` (or `FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR>`)
and sends it. Authentication is `firebase login`, or `GOOGLE_APPLICATION_CREDENTIALS` (service account with App
Distribution Admin). `--bump` raises the flavor's `versionCode` (build-kit's `scripts/bump-version-code.sh`, nothing
is committed); `--dry-run` builds and checks and sends nothing. With neither groups nor testers the build is uploaded
and sent to nobody. A non-production variant is signed with the shared debug keystore, else AGP's debug key
(build-kit), so it installs. The script needs build-kit's `scripts/app-properties.sh`, `bump-version-code.sh` and
`make build-android`.

**iOS** goes through fastlane: the app's `fastlane/Fastfile` (build-kit's) has `import "FirebaseFastfile"`;
`fastlane/Pluginfile` has `gem "fastlane-plugin-firebase_app_distribution"`. The same Fastfile has an Android lane
(`bundle exec fastlane android firebase …`) for a pipeline that already runs fastlane.

- Environment: `GOOGLE_APPLICATION_CREDENTIALS`, `FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR>` (optional on Android),
  `FIREBASE_APP_ID_IOS_<FLAVOR>`, optional `FIREBASE_GROUPS` (the default for `--groups`); for iOS also
  `IOS_BUNDLE_ID_<FLAVOR>`, `APP_STORE_CONNECT_API_KEY_PATH`, match. They can sit in `fastlane/.env.firebase` (git-ignored).
- The production flavor is refused — stores only.
- A group must exist in the console before it can be addressed (App Distribution → Testers & Groups).
