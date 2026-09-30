import FirebaseMessaging
import Foundation
import SampleShared
import UserNotifications

/// Cloud Messaging for the Kotlin `PushMessaging` and `PushMessageStream`: the token and topics, and
/// every message and tap delivered to `PushHub`.
final class MessagingBridgeImpl: NSObject, MessagingBridge {
    static let shared = MessagingBridgeImpl()

    private static let platformKeys = ["aps", "gcm.", "google.", "fcm_options"]

    func token(onComplete: @escaping (String?, String?) -> Void) {
        Messaging.messaging().token { token, error in
            onComplete(token, error?.localizedDescription ?? (token == nil ? "No push token yet" : nil))
        }
    }

    func subscribe(topic: String, onComplete: @escaping (String?) -> Void) {
        Messaging.messaging().subscribe(toTopic: topic) { error in onComplete(error?.localizedDescription) }
    }

    func unsubscribe(topic: String, onComplete: @escaping (String?) -> Void) {
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in onComplete(error?.localizedDescription) }
    }

    func received(userInfo: [AnyHashable: Any], title: String?, body: String?) {
        PushHub.shared.receive(title: title ?? alert(userInfo, "title"), body: body ?? alert(userInfo, "body"), data: data(userInfo))
    }

    func opened(userInfo: [AnyHashable: Any], title: String?, body: String?) {
        PushHub.shared.open(title: title ?? alert(userInfo, "title"), body: body ?? alert(userInfo, "body"), data: data(userInfo))
    }

    /// The custom payload: every string-keyed entry that is not APNs' or Firebase's own.
    private func data(_ userInfo: [AnyHashable: Any]) -> [String: String] {
        var result: [String: String] = [:]
        for (key, value) in userInfo {
            guard let key = key as? String,
                  !Self.platformKeys.contains(where: { key.hasPrefix($0) }) else { continue }
            result[key] = "\(value)"
        }
        return result
    }

    private func alert(_ userInfo: [AnyHashable: Any], _ field: String) -> String? {
        let alert = (userInfo["aps"] as? [String: Any])?["alert"]
        if let alert = alert as? [String: Any] { return alert[field] as? String }
        return field == "body" ? alert as? String : nil
    }
}

extension MessagingBridgeImpl: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken else { return }
        PushHub.shared.refreshToken(token: fcmToken)
    }
}

extension MessagingBridgeImpl: UNUserNotificationCenterDelegate {
    /// A notification arriving while the app is open: delivered to Kotlin and shown as a banner.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let content = notification.request.content
        received(userInfo: content.userInfo, title: content.title, body: content.body)
        completionHandler([.banner, .sound, .badge])
    }

    /// A tap on a notification.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let content = response.notification.request.content
        opened(userInfo: content.userInfo, title: content.title, body: content.body)
        completionHandler()
    }
}
