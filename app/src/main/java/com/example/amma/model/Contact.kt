package com.example.amma.model

import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val relationship: String, // e.g. "Son", "Daughter", "Husband", "Brother", "Doctor"
    val photoUri: String? = null, // Local file path or drawable resource name
    val photoDrawableRes: Int? = null, // Built-in fallback drawable or avatar
    val phoneNumber: String,
    val whatsappNumber: String = phoneNumber,
    val primaryTransport: CallTransport = CallTransport.CELLULAR,
    val allowWhatsappAudio: Boolean = true,
    val allowWhatsappVideo: Boolean = true,
    val customPronunciation: String? = null, // Telugu pronunciation override e.g. "సంతోష్"
    val sortOrder: Int = 0,
    val isEmergencyContact: Boolean = false
) {
    val effectivePronunciation: String
        get() = if (!customPronunciation.isNullOrBlank() && !customPronunciation.equals("null", ignoreCase = true)) {
            customPronunciation.trim()
        } else {
            displayName
        }
}
