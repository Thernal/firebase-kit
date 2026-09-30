import FirebaseRemoteConfig
import Foundation
import SampleShared

/// Remote Config for the Kotlin `RemoteConfigSource`. Values cross as strings; Kotlin decodes them
/// to each key's type.
final class RemoteConfigBridgeImpl: RemoteConfigBridge {
    private let remoteConfig = RemoteConfig.remoteConfig()

    func configure(minimumFetchIntervalSeconds: Int64, fetchTimeoutSeconds: Int64) {
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = TimeInterval(minimumFetchIntervalSeconds)
        settings.fetchTimeout = TimeInterval(fetchTimeoutSeconds)
        remoteConfig.configSettings = settings
    }

    func activatedValues() -> [String: String] {
        values()
    }

    func fetchAndActivate(onComplete: @escaping ([String: String]?, String?) -> Void) {
        remoteConfig.fetchAndActivate { [weak self] _, error in
            if let error {
                onComplete(nil, error.localizedDescription)
            } else {
                onComplete(self?.values() ?? [:], nil)
            }
        }
    }

    private func values() -> [String: String] {
        var result: [String: String] = [:]
        for key in remoteConfig.allKeys(from: .remote) {
            result[key] = remoteConfig.configValue(forKey: key).stringValue
        }
        return result
    }
}
