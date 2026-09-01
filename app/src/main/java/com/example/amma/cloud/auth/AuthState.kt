package com.example.amma.cloud.auth

/**
 * Authentication State of the Caregiver managing the CareTouch device.
 */
sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?
    ) : AuthState
    data class Error(val message: String) : AuthState
}
