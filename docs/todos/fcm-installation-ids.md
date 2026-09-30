# FCM registration by installation id

**Status:** open

Firebase iOS SDK 12.19 deprecates registration tokens (`Messaging.token`, `didReceiveRegistrationToken`)
in favour of registering the Firebase Installation ID (`register(completion:)`,
`messaging(_:didReceiveRegistration:)`), opt-in through `FirebaseMessagingInstallationIdEnabled` in
Info.plist. The kit uses tokens — they still work by default, with a deprecation warning in
`MessagingBridgeImpl.swift`. Moving means the backend sends to installation ids instead of tokens; decide
it together with the backend, then switch both platforms (and `PushMessaging.tokens`' meaning) at once.
