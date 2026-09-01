package com.example.amma.model

sealed interface CallState {
    object Idle : CallState

    data class ContactOptionsPicker(
        val contact: Contact
    ) : CallState

    data class PreparingCall(
        val contact: Contact,
        val transport: CallTransport
    ) : CallState

    data class Calling(
        val contact: Contact,
        val transport: CallTransport,
        val isEmergency: Boolean = false
    ) : CallState

    data class Connected(
        val contact: Contact,
        val transport: CallTransport
    ) : CallState

    data class CallFailed(
        val contact: Contact,
        val reason: FailureReason,
        val teluguMessage: String
    ) : CallState

    data class FallbackPrompt(
        val contact: Contact,
        val originalTransport: CallTransport,
        val suggestedTransport: CallTransport,
        val reasonMessageTelugu: String
    ) : CallState

    data class EmergencyTriggering(
        val contact: Contact,
        val progress: Float // 0.0f to 1.0f during hold
    ) : CallState
}

enum class FailureReason {
    NO_CELLULAR_SIGNAL,
    NO_INTERNET,
    WHATSAPP_NOT_INSTALLED,
    PERMISSION_DENIED,
    INVALID_NUMBER,
    UNKNOWN
}
