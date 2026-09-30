# App Distribution

```sh
scripts/distribute-firebase.sh android <app> <flavor> [groups]
scripts/distribute-firebase.sh ios <flavor> [groups]
```

- The app's `fastlane/Fastfile` (build-kit's) has `import "FirebaseFastfile"`; `fastlane/Pluginfile` has
  `gem "fastlane-plugin-firebase_app_distribution"`.
- Environment: `GOOGLE_APPLICATION_CREDENTIALS` (service account with App Distribution Admin),
  `FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR>`, `FIREBASE_APP_ID_IOS_<FLAVOR>`, optional `FIREBASE_GROUPS`;
  for iOS also `IOS_BUNDLE_ID_<FLAVOR>`, `APP_STORE_CONNECT_API_KEY_PATH`, match.
- The build goes through `make build-android` / `make build-ios-framework`; release notes default to the
  last commit subject.
- The production flavor is refused — stores only.
