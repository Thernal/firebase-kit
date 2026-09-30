package io.thernal.firebasekit.firebase.auth.impl.data

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import io.thernal.firebasekit.firebase.auth.api.data.SignInResult
import io.thernal.firebasekit.firebase.auth.api.data.SocialSignIn
import kotlinx.coroutines.tasks.await

actual fun platformSocialSignIn(): SocialSignIn {
    return AndroidSocialSignIn
}

/**
 * Google through Credential Manager (its "Sign in with Google" sheet), Apple through Firebase's web
 * flow for `apple.com`. The Google server client id is the `default_web_client_id` string the
 * google-services Gradle plugin generates from google-services.json.
 */
internal object AndroidSocialSignIn : SocialSignIn {
    private const val APPLE_PROVIDER = "apple.com"
    private const val WEB_CLIENT_ID = "default_web_client_id"
    private const val WEB_CONTEXT_CANCELLED = "ERROR_WEB_CONTEXT_CANCELED"

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    override suspend fun signInWithGoogle(): SignInResult {
        val activity = ResumedActivity.activity ?: return SignInResult.Failed("No activity on screen")
        val firebase = auth ?: return SignInResult.Failed("Firebase is not configured")
        val clientId = activity.webClientId() ?: return SignInResult.Failed(
            "No $WEB_CLIENT_ID resource (google-services plugin)",
        )
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = clientId).build())
            .build()
        return try {
            val credential = CredentialManager.create(
                activity,
            ).getCredential(context = activity, request = request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return SignInResult.Failed("Unexpected credential type")
            }
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            firebase.complete(GoogleAuthProvider.getCredential(idToken, null))
        } catch (_: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (failure: GetCredentialException) {
            SignInResult.Failed("Google sign-in failed: ${failure.message.orEmpty()}")
        } catch (failure: FirebaseException) {
            SignInResult.Failed("Firebase sign-in failed: ${failure.message.orEmpty()}")
        }
    }

    override suspend fun signInWithApple(): SignInResult {
        val activity = ResumedActivity.activity ?: return SignInResult.Failed("No activity on screen")
        val firebase = auth ?: return SignInResult.Failed("Firebase is not configured")
        val provider = OAuthProvider.newBuilder(APPLE_PROVIDER).build()
        return try {
            // A flow the system killed the app during comes back as the pending result.
            val result = firebase.pendingAuthResult?.await()
                ?: firebase.startActivityForSignInWithProvider(activity, provider).await()
            result.toSignInResult()
        } catch (failure: FirebaseException) {
            if (failure.message.orEmpty().contains(WEB_CONTEXT_CANCELLED)) {
                SignInResult.Cancelled
            } else {
                SignInResult.Failed("Apple sign-in failed: ${failure.message.orEmpty()}")
            }
        }
    }

    override suspend fun signOut() {
        auth?.signOut()
        val activity = ResumedActivity.activity ?: return
        try {
            CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            // Nothing stored to clear, or no provider: the Firebase sign-out above is what counts.
        }
    }

    private suspend fun FirebaseAuth.complete(credential: AuthCredential): SignInResult {
        return signInWithCredential(credential).await().toSignInResult()
    }

    private suspend fun AuthResult.toSignInResult(): SignInResult {
        val user = user ?: return SignInResult.Failed("No Firebase user")
        val token = user.getIdToken(false).await().token ?: return SignInResult.Failed("No Firebase ID token")
        return SignInResult.Success(idToken = token, userId = user.uid)
    }

    private fun Activity.webClientId(): String? {
        // Looked up by name: the resource is generated into the app, not into this module.
        @Suppress("DiscouragedApi")
        val id = resources.getIdentifier(WEB_CLIENT_ID, "string", packageName)
        return id.takeIf { it != 0 }?.let(::getString)
    }
}
