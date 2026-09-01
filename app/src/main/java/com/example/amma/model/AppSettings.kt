package com.example.amma.model

data class AppSettings(
    val adminPin: String = "1234",
    val isHighAssistanceMode: Boolean = true,
    val speakTimeOnTap: Boolean = true,
    val speakBatteryOnTap: Boolean = true,
    val speakSignalOnTap: Boolean = true,
    val speakContactNameOnTap: Boolean = true,
    val speechRate: Float = 0.95f, // Slightly measured pacing for elder clarity
    val speechPitch: Float = 1.0f,
    val neuralVoice: String = "te-IN-ShrutiNeural", // Azure Neural Voice: Shruti (Female) or Mohan (Male)
    val emergencyContactId: String? = null,
    val defaultSimSlot: Int = 0 // 0 for SIM 1, 1 for SIM 2
)
