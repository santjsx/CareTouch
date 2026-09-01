package com.example.amma.model

enum class CallTransport(val displayName: String, val teluguLabel: String) {
    CELLULAR("Phone Call", "సాధారణ ఫోన్"),
    WHATSAPP_AUDIO("WhatsApp Audio", "వాట్సాప్ ఆడియో"),
    WHATSAPP_VIDEO("WhatsApp Video", "వాట్సాప్ వీడియో")
}
