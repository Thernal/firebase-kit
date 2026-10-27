// firebase-kit: the Realtime Database's Swift bridge. A part of its own (ios/FirebaseDatabase) beside
// ios/Firebase, so an app that does not use the database leaves it out and needs no FirebaseDatabase package.

import FirebaseDatabase
import Foundation
import SampleShared

extension FirebaseKit {
    /// After `configure`, in an app that uses the Realtime Database.
    static func configureDatabase() {
        FirebaseBridges.shared.registerDatabase(database: DatabaseBridgeImpl())
    }
}

/// The Realtime Database for the Kotlin `RealtimeDatabase`. Values cross as JSON text; Kotlin parses it.
final class DatabaseBridgeImpl: DatabaseBridge {
    private let database = Database.database()

    func pushKey() -> String {
        database.reference().childByAutoId().key ?? UUID().uuidString
    }

    func set(path: String, json: String, onComplete: @escaping (DatabaseBridgeError?) -> Void) {
        guard let value = Self.value(json, onComplete: onComplete) else { return }
        database.reference(withPath: path).setValue(value) { error, _ in onComplete(Self.bridgeError(error)) }
    }

    func update(path: String, json: String, onComplete: @escaping (DatabaseBridgeError?) -> Void) {
        guard let value = Self.value(json, onComplete: onComplete) else { return }
        guard let values = value as? [AnyHashable: Any] else {
            onComplete(DatabaseBridgeError(message: "An update takes a JSON object", isPermissionDenied: false))
            return
        }
        database.reference(withPath: path).updateChildValues(values) { error, _ in onComplete(Self.bridgeError(error)) }
    }

    func remove(path: String, onComplete: @escaping (DatabaseBridgeError?) -> Void) {
        database.reference(withPath: path).removeValue { error, _ in onComplete(Self.bridgeError(error)) }
    }

    func get(path: String, onComplete: @escaping (String?, DatabaseBridgeError?) -> Void) {
        database.reference(withPath: path).getData { error, snapshot in
            if let error {
                onComplete(nil, Self.bridgeError(error))
            } else {
                onComplete(Self.json(snapshot?.value), nil)
            }
        }
    }

    func observe(
        path: String,
        onValue: @escaping (String?) -> Void,
        onError: @escaping (DatabaseBridgeError) -> Void
    ) -> DatabaseObservation {
        let reference = database.reference(withPath: path)
        let handle = reference.observe(
            .value,
            with: { snapshot in onValue(Self.json(snapshot.value)) },
            withCancel: { error in onError(Self.bridgeError(error) ?? Self.unknownError) }
        )
        return Observation(reference: reference, handle: handle)
    }

    private static let unknownError = DatabaseBridgeError(message: "The database failed", isPermissionDenied: false)

    /// The JSON text Kotlin sent as Foundation values; a malformed one completes the call with an error.
    private static func value(_ json: String, onComplete: (DatabaseBridgeError?) -> Void) -> Any? {
        do {
            return try JSONSerialization.jsonObject(with: Data(json.utf8), options: [.fragmentsAllowed])
        } catch {
            onComplete(DatabaseBridgeError(message: error.localizedDescription, isPermissionDenied: false))
            return nil
        }
    }

    /// A snapshot's value as JSON text; `nil` when there is none (`NSNull`).
    private static func json(_ value: Any?) -> String? {
        guard let value, !(value is NSNull),
              let data = try? JSONSerialization.data(withJSONObject: value, options: [.fragmentsAllowed]) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    /// The SDK's errors are in the "com.firebase" domain; code 1 is "permission_denied" (FUtilities.m).
    private static func bridgeError(_ error: Error?) -> DatabaseBridgeError? {
        guard let error = error as NSError? else { return nil }
        let isPermissionDenied = (error.domain == "com.firebase" && error.code == 1)
            || error.localizedDescription.localizedCaseInsensitiveContains("permission denied")
        return DatabaseBridgeError(message: error.localizedDescription, isPermissionDenied: isPermissionDenied)
    }
}

private final class Observation: DatabaseObservation {
    private let reference: DatabaseReference
    private let handle: DatabaseHandle

    init(reference: DatabaseReference, handle: DatabaseHandle) {
        self.reference = reference
        self.handle = handle
    }

    func cancel() {
        reference.removeObserver(withHandle: handle)
    }
}
