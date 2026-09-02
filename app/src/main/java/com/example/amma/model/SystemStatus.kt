package com.example.amma.model

enum class BatteryLevelGrade(val teluguPhrase: String, val isCritical: Boolean) {
    EXCELLENT("బ్యాటరీ బాగుంది", false),
    GOOD("బ్యాటరీ బాగుంది", false),
    MEDIUM("బ్యాటరీ సగం కంటే తక్కువగా ఉంది", false),
    LOW("బ్యాటరీ తక్కువగా ఉంది", true),
    CRITICAL("బ్యాటరీ చాలా తక్కువగా ఉంది. ఛార్జర్ పెట్టండి", true),
    CHARGING("బ్యాటరీ ఛార్జ్ అవుతోంది", false)
}

enum class SignalGrade(val teluguPhrase: String) {
    EXCELLENT("ఫోన్ సిగ్నల్ చాలా బాగుంది"),
    GOOD("ఫోన్ సిగ్నల్ బాగుంది"),
    MODERATE("ఫోన్ సిగ్నల్ పర్వాలేదు"),
    POOR("ఫోన్ సిగ్నల్ కొంచెం తక్కువగా ఉంది"),
    NO_SIGNAL("ఫోన్ సిగ్నల్ లేదు"),
    AIRPLANE_MODE("ఫోన్ ఎయిర్‌ప్లేన్ మోడ్‌లో ఉంది")
}

data class SystemStatus(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val batteryGrade: BatteryLevelGrade = BatteryLevelGrade.EXCELLENT,
    val signalBars: Int = 4,
    val signalGrade: SignalGrade = SignalGrade.EXCELLENT,
    val isSimAvailable: Boolean = true,
    val isInternetAvailable: Boolean = true,
    val isWifiConnected: Boolean = true,
    val isDataDenied: Boolean = false,
    val isWhatsAppInstalled: Boolean = true,
    val formattedTime: String = "10:42 AM",
    val formattedDate: String = "మంగళవారం, సెప్టెంబర్ 1",
    val teluguTimePhrase: String = "ఇప్పుడు ఉదయం పది గంటల నలభై రెండు నిమిషాలు",
    val teluguDatePhrase: String = "ఈరోజు మంగళవారం, సెప్టెంబర్ ఒకటో తేదీ"
)
