// firebase-kit: the app's side of the Swift bridges. Installed into the Xcode project by
// `skillctl.sh kit install firebase-kit --place ios/Firebase=<the app's source folder>/Firebase`,
// with the Kotlin framework import below renamed to the app's own.

import FirebaseCore
import FirebaseMessaging
import GoogleSignIn
import SampleShared
import UIKit
import UserNotifications

/// Configures Firebase and hands the Kotlin side its bridges. Call the four entry points from the
/// app delegate; see `firebase/README.md` → iOS setup.
enum FirebaseKit {
    /// In `application(_:didFinishLaunchingWithOptions:)`, before anything Kotlin uses Firebase.
    /// Pass `false` for a capability the app leaves out; its Kotlin side then does nothing.
    static func configure(
        application: UIApplication,
        launchOptions: [UIApplication.LaunchOptionsKey: Any]?,
        crashlytics: Bool = true,
        remoteConfig: Bool = true,
        messaging: Bool = true,
        auth: Bool = true
    ) {
        FirebaseApp.configure()
        FirebaseBridges.shared.register(
            crashlytics: crashlytics ? CrashlyticsBridgeImpl() : nil,
            remoteConfig: remoteConfig ? RemoteConfigBridgeImpl() : nil,
            messaging: messaging ? MessagingBridgeImpl.shared : nil,
            auth: auth ? AuthBridgeImpl() : nil
        )
        guard messaging else { return }
        UNUserNotificationCenter.current().delegate = MessagingBridgeImpl.shared
        Messaging.messaging().delegate = MessagingBridgeImpl.shared
        application.registerForRemoteNotifications()
        // Launched by a tap on a notification while the app was not running.
        if let userInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            MessagingBridgeImpl.shared.opened(userInfo: userInfo, title: nil, body: nil)
        }
    }

    /// In `application(_:didRegisterForRemoteNotificationsWithDeviceToken:)`.
    static func didRegisterForRemoteNotifications(deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    /// In `application(_:didReceiveRemoteNotification:fetchCompletionHandler:)` — data messages.
    static func didReceiveRemoteNotification(userInfo: [AnyHashable: Any]) {
        MessagingBridgeImpl.shared.received(userInfo: userInfo, title: nil, body: nil)
    }

    /// In `application(_:open:options:)`: the Google Sign-In redirect. `true` when it was one.
    static func open(url: URL) -> Bool {
        GIDSignIn.sharedInstance.handle(url)
    }
}
