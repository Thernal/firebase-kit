import AuthenticationServices
import CryptoKit
import FirebaseAuth
import FirebaseCore
import Foundation
import GoogleSignIn
import SampleShared
import UIKit

/// Google Sign-In and Sign in with Apple for the Kotlin `SocialSignIn`: each ends in a Firebase sign-in
/// and calls back with the Firebase ID token.
final class AuthBridgeImpl: NSObject, AuthBridge {
    private typealias Completion = (String?, KotlinBoolean, String?) -> Void

    private var appleCompletion: Completion?
    private var appleNonce: String?

    func signInWithGoogle(onComplete: @escaping (String?, KotlinBoolean, String?) -> Void) {
        DispatchQueue.main.async {
            guard let presenter = Self.topViewController() else {
                return onComplete(nil, KotlinBoolean(bool: false), "No view controller to present from")
            }
            guard let clientID = FirebaseApp.app()?.options.clientID else {
                return onComplete(nil, KotlinBoolean(bool: false), "No Firebase client id (GoogleService-Info.plist)")
            }
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
            GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
                if let error = error as NSError? {
                    let isCancelled = error.code == GIDSignInError.canceled.rawValue
                    return onComplete(nil, KotlinBoolean(bool: isCancelled), isCancelled ? nil : error.localizedDescription)
                }
                guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                    return onComplete(nil, KotlinBoolean(bool: false), "Google returned no ID token")
                }
                let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: user.accessToken.tokenString)
                Self.completeFirebaseSignIn(credential: credential, onComplete: onComplete)
            }
        }
    }

    func signInWithApple(onComplete: @escaping (String?, KotlinBoolean, String?) -> Void) {
        DispatchQueue.main.async {
            guard self.appleCompletion == nil else {
                return onComplete(nil, KotlinBoolean(bool: false), "Sign in with Apple is already in progress")
            }
            let nonce = Self.randomNonce()
            self.appleNonce = nonce
            self.appleCompletion = onComplete
            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = Self.sha256(nonce)
            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            controller.performRequests()
        }
    }

    func signOut() {
        GIDSignIn.sharedInstance.signOut()
        try? Auth.auth().signOut()
    }

    private func finishApple(token: String?, isCancelled: Bool, error: String?) {
        let completion = appleCompletion
        appleCompletion = nil
        appleNonce = nil
        completion?(token, KotlinBoolean(bool: isCancelled), error)
    }

    private static func completeFirebaseSignIn(credential: AuthCredential, onComplete: @escaping Completion) {
        Auth.auth().signIn(with: credential) { result, error in
            guard let user = result?.user else {
                return onComplete(nil, KotlinBoolean(bool: false), error?.localizedDescription ?? "Firebase sign-in failed")
            }
            user.getIDToken { token, error in
                onComplete(token, KotlinBoolean(bool: false), token == nil ? (error?.localizedDescription ?? "No Firebase ID token") : nil)
            }
        }
    }

    private static func topViewController() -> UIViewController? {
        let window = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)
        var top = window?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }

    private static func randomNonce(length: Int = 32) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        return String((0..<length).map { _ in charset[Int.random(in: 0..<charset.count)] })
    }

    private static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }
}

extension AuthBridgeImpl: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let appleCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let nonce = appleNonce,
              let tokenData = appleCredential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            return finishApple(token: nil, isCancelled: false, error: "Apple returned no identity token")
        }
        let credential = OAuthProvider.appleCredential(withIDToken: idToken, rawNonce: nonce, fullName: appleCredential.fullName)
        Self.completeFirebaseSignIn(credential: credential) { [weak self] token, _, error in
            self?.finishApple(token: token, isCancelled: false, error: error)
        }
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let isCancelled = (error as? ASAuthorizationError)?.code == .canceled
        finishApple(token: nil, isCancelled: isCancelled, error: isCancelled ? nil : error.localizedDescription)
    }
}

extension AuthBridgeImpl: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow) ?? ASPresentationAnchor()
    }
}
