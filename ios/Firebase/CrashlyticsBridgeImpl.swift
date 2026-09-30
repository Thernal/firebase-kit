import FirebaseCrashlytics
import Foundation
import SampleShared

/// Crashlytics for the Kotlin `CrashReporter`.
final class CrashlyticsBridgeImpl: CrashlyticsBridge {
    private let crashlytics = Crashlytics.crashlytics()

    func setCollectionEnabled(enabled: Bool) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    func setUserId(userId: String) {
        crashlytics.setUserID(userId)
    }

    func log(message: String) {
        crashlytics.log(message)
    }

    func setCustomKey(key: String, value: String) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    /// A Kotlin throwable as a non-fatal: its class names the issue, the Kotlin stack trace travels as
    /// the report's first log lines (Crashlytics cannot symbolicate Kotlin frames itself).
    func recordError(type: String, message: String, stackTrace: String) {
        crashlytics.log(stackTrace)
        let error = NSError(
            domain: type,
            code: 0,
            userInfo: [NSLocalizedDescriptionKey: message.isEmpty ? type : message]
        )
        crashlytics.record(error: error)
    }
}
