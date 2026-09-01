package com.example.amma.cloud.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Modern Firebase Authentication Repository using Android's Credential Manager API.
 */
class AuthRepository(private val context: Context) {

    private val appContext = context.applicationContext
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(appContext)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(
                    uid = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString()
                )
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Launches Google Sign-In sheet using Android 14+ Credential Manager API.
     * Auto-resolves Web Client ID from generated resources without manual configuration.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String = ""
    ): Result<AuthState.Authenticated> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading

        try {
            val clientId = if (serverClientId.isNotBlank()) {
                serverClientId
            } else {
                val resId = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
                if (resId != 0) activityContext.getString(resId) else "113957510587-gucvc9db5c2qjhhi1mf5765raq6hsgf3.apps.googleusercontent.com"
            }

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = withContext(Dispatchers.Main) {
                credentialManager.getCredential(
                    request = request,
                    context = activityContext
                )
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in to Firebase with Google Id Token
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                val user = authResult.user
                if (user != null) {
                    val authUser = AuthState.Authenticated(
                        uid = user.uid,
                        displayName = user.displayName,
                        email = user.email,
                        photoUrl = user.photoUrl?.toString()
                    )
                    _authState.value = authUser
                    Result.success(authUser)
                } else {
                    val errorState = AuthState.Error("Firebase returned null user")
                    _authState.value = errorState
                    Result.failure(Exception("Firebase returned null user"))
                }
            } else {
                val errorState = AuthState.Error("Unexpected credential type")
                _authState.value = errorState
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In")
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error", e)
            val errorState = AuthState.Error("Sign in failed: ${e.message}")
            _authState.value = errorState
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In exception", e)
            val errorState = AuthState.Error("Sign in error: ${e.localizedMessage ?: "Unknown error"}")
            _authState.value = errorState
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth.signOut()
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
