package com.example.amma.voice

import com.example.amma.model.BatteryLevelGrade
import com.example.amma.model.CallTransport
import com.example.amma.model.SignalGrade
import java.util.Calendar

/**
 * Native Telugu Spoken Phrase Resolver
 * Pure Telugu engine ensuring natural, polite, and authentic Telugu voice output for elders.
 */
object TeluguPhraseResolver {

    private val teluguUnits = mapOf(
        0 to "సున్నా", 1 to "ఒకటి", 2 to "రెండు", 3 to "మూడు", 4 to "నాలుగు",
        5 to "ఐదు", 6 to "ఆరు", 7 to "ఏడు", 8 to "ఎనిమిది", 9 to "తొమ్మిది",
        10 to "పది", 11 to "పదకొండు", 12 to "పన్నెండు", 13 to "పదమూడు", 14 to "పద్నాలుగు",
        15 to "పదిహేను", 16 to "పదహారు", 17 to "పదిహేడు", 18 to "పద్దెనిమిది", 19 to "పంతొమ్మిది"
    )

    private val teluguTens = mapOf(
        20 to "ఇరవై", 30 to "ముప్పై", 40 to "నలభై", 50 to "యాభై",
        60 to "అరవై", 70 to "డెబ్బై", 80 to "ఎనభై", 90 to "తొంభై"
    )

    private val teluguDays = arrayOf(
        "", "ఆదివారం", "సోమవారం", "మంగళవారం", "బుధవారం", "గురువారం", "శుక్రవారం", "శనివారం"
    )

    private val teluguMonths = arrayOf(
        "జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
        "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"
    )

    // Common English names and words mapped to accurate Telugu phonetic text
    private val commonWordsToTelugu = mapOf(
        "santhosh" to "సంతోష్",
        "santosh" to "సంతోష్",
        "swapna" to "స్వప్న",
        "ramesh" to "రమేష్",
        "srinu" to "శ్రీను",
        "srinivas" to "శ్రీనివాస్",
        "sreenivas" to "శ్రీనివాస్",
        "doctor" to "డాక్టర్",
        "dr" to "డాక్టర్",
        "amma" to "అమ్మ",
        "nanna" to "నాన్న",
        "akka" to "అక్క",
        "annayya" to "అన్నయ్య",
        "anna" to "అన్న",
        "chelli" to "చెల్లి",
        "thammudu" to "తమ్ముడు",
        "son" to "కొడుకు",
        "daughter" to "కూతురు",
        "hospital" to "హాస్పిటల్",
        "police" to "పోలీస్",
        "ambulance" to "అంబులెన్స్",
        "emergency" to "ఎమర్జెన్సీ",
        "kiran" to "కిరణ్",
        "rajesh" to "రాజేష్",
        "priya" to "ప్రియ",
        "suresh" to "సురేష్",
        "mohan" to "మోహన్",
        "venkat" to "వెంకట్",
        "rao" to "రావు",
        "reddy" to "రెడ్డి",
        "kumar" to "కుమార్",
        "prasad" to "ప్రసాద్",
        "babu" to "బాబు",
        "lakshmi" to "లక్ష్మి",
        "padma" to "పద్మ",
        "vani" to "వాణి",
        "geetha" to "గీత",
        "gita" to "గీత",
        "sai" to "సాయి",
        "anita" to "అనిత",
        "kavitha" to "కవిత",
        "manju" to "మంజు",
        "sunitha" to "సునీత",
        "roopa" to "రూప",
        "deepa" to "దీప",
        "radha" to "రాధ",
        "shanti" to "శాంతి",
        "ramu" to "రాము",
        "krishna" to "కృష్ణ",
        "vijay" to "విజయ్",
        "ravi" to "రవి",
        "anand" to "ఆనంద్",
        "praveen" to "ప్రవీణ్",
        "madhu" to "మధు",
        "satish" to "సతీష్",
        "naresh" to "నరేష్",
        "siva" to "శివ",
        "shiva" to "శివ",
        "harish" to "హరీష్",
        "ganesh" to "గణేష్",
        "mahesh" to "మహేష్",
        "bala" to "బాల",
        "whatsapp" to "వాట్సాప్",
        "call" to "కాల్",
        "video" to "వీడియో",
        "audio" to "ఆడియో",
        "battery" to "బ్యాటరీ",
        "signal" to "సిగ్నల్",
        "internet" to "ఇంటర్నెట్",
        "sim" to "సిమ్",
        "mobile" to "మొబైల్"
    )

    /**
     * Transliterates any English name/word to natural Telugu script
     */
    fun transliterateToTelugu(word: String): String {
        val trimmed = word.trim()
        if (trimmed.isBlank()) return ""

        // If already contains Telugu characters, return as-is
        if (trimmed.any { it in '\u0C00'..'\u0C7F' }) {
            return trimmed
        }

        val lower = trimmed.lowercase()
        commonWordsToTelugu[lower]?.let { return it }

        // Syllable transliteration for arbitrary English names
        return phoneticTransliterate(lower)
    }

    private fun phoneticTransliterate(input: String): String {
        var s = input
            .replace("sh", "ష్")
            .replace("th", "త్")
            .replace("ch", "చ్")
            .replace("kh", "ఖ్")
            .replace("gh", "ఘ్")
            .replace("ph", "ఫ్")
            .replace("bh", "భ్")
            .replace("dh", "ధ్")
            .replace("aa", "ా")
            .replace("ee", "ీ")
            .replace("oo", "ూ")
            .replace("ou", "ౌ")
            .replace("ai", "ై")
            .replace("au", "ౌ")
            .replace("a", "ా")
            .replace("e", "ె")
            .replace("i", "ి")
            .replace("o", "ొ")
            .replace("u", "ు")
            .replace("k", "క్")
            .replace("g", "గ్")
            .replace("c", "క్")
            .replace("j", "జ్")
            .replace("t", "ట్")
            .replace("d", "డ్")
            .replace("n", "న్")
            .replace("p", "ప్")
            .replace("b", "బ్")
            .replace("m", "మ్")
            .replace("y", "య్")
            .replace("r", "ర్")
            .replace("l", "ల్")
            .replace("v", "వ్")
            .replace("w", "వ్")
            .replace("s", "స్")
            .replace("h", "హ్")
            .replace("z", "జ్")

        // Clean up any remaining Latin characters
        return s.filter { it in '\u0C00'..'\u0C7F' || it.isWhitespace() || it in ".,!?" }
    }

    /**
     * Ensures all text is 100% pure Telugu words, converting any Latin text or digits to pure Telugu.
     */
    fun sanitizeForSpeech(text: String): String {
        if (text.isBlank()) return ""

        // Remove parenthesized info like (e.g. Son / కొడుకు)
        val withoutParentheses = text.replace(Regex("\\s*\\([^)]*\\)"), "")

        // Split into tokens to transliterate English words into Telugu
        val tokens = withoutParentheses.split(Regex("(?<=[\\s.,!?])|(?=[\\s.,!?])"))
        val builder = StringBuilder()

        for (token in tokens) {
            val trimmed = token.trim()
            if (trimmed.isEmpty()) {
                builder.append(token)
                continue
            }

            // Check if token is digits
            val num = trimmed.toIntOrNull()
            if (num != null) {
                builder.append(numberToTelugu(num))
            } else if (trimmed.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                // English word -> convert to Telugu phonetics
                builder.append(transliterateToTelugu(trimmed))
            } else {
                // Already Telugu or punctuation
                builder.append(trimmed)
            }
        }

        return builder.toString().trim()
    }

    fun numberToTelugu(num: Int): String {
        val safeNum = num.coerceIn(0, 100)
        if (safeNum in 0..19) {
            return teluguUnits[safeNum] ?: safeNum.toString()
        }
        if (safeNum == 100) return "వంద"
        if (safeNum in 20..99) {
            val tens = (safeNum / 10) * 10
            val ones = safeNum % 10
            val tensWord = teluguTens[tens] ?: return safeNum.toString()
            return if (ones == 0) {
                tensWord
            } else {
                val onesWord = teluguUnits[ones] ?: ones.toString()
                "$tensWord $onesWord"
            }
        }
        return safeNum.toString()
    }

    fun getFullDateTimePhrase(calendar: Calendar = Calendar.getInstance()): String {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        val dayName = teluguDays.getOrElse(dayOfWeek) { "ఈరోజు" }
        val monthName = teluguMonths.getOrElse(month) { "" }
        val dayNumTelugu = numberToTelugu(dayOfMonth)

        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12Raw = calendar.get(Calendar.HOUR)
        val hour12 = if (hour12Raw == 0) 12 else hour12Raw
        val minute = calendar.get(Calendar.MINUTE)

        val timePeriod = when (hour24) {
            in 4..11 -> "ఉదయం"
            in 12..15 -> "మధ్యాహ్నం"
            in 16..19 -> "సాయంత్రం"
            else -> "రాత్రి"
        }

        val hourTelugu = numberToTelugu(hour12)
        val timeText = if (minute == 0) {
            "$timePeriod $hourTelugu గంటలు అయింది"
        } else {
            val minuteTelugu = numberToTelugu(minute)
            "$timePeriod $hourTelugu గంటల $minuteTelugu నిమిషాలు"
        }

        return "ఇప్పుడు $timeText. ఈరోజు $dayName, $monthName ${dayNumTelugu}వ తారీఖు."
    }

    fun getTimePhrase(calendar: Calendar = Calendar.getInstance()): String {
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12Raw = calendar.get(Calendar.HOUR)
        val hour12 = if (hour12Raw == 0) 12 else hour12Raw
        val minute = calendar.get(Calendar.MINUTE)

        val timePeriod = when (hour24) {
            in 4..11 -> "ఉదయం"
            in 12..15 -> "మధ్యాహ్నం"
            in 16..19 -> "సాయంత్రం"
            else -> "రాత్రి"
        }

        val hourTelugu = numberToTelugu(hour12)

        return if (minute == 0) {
            "ఇప్పుడు $timePeriod $hourTelugu గంటలు అయింది."
        } else {
            val minuteTelugu = numberToTelugu(minute)
            "ఇప్పుడు $timePeriod $hourTelugu గంటల $minuteTelugu నిమిషాలు."
        }
    }

    fun getDatePhrase(calendar: Calendar = Calendar.getInstance()): String {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        val dayName = teluguDays.getOrElse(dayOfWeek) { "ఈరోజు" }
        val monthName = teluguMonths.getOrElse(month) { "" }
        val dayNumTelugu = numberToTelugu(dayOfMonth)

        return "ఈరోజు $dayName, $monthName ${dayNumTelugu}వ తారీఖు."
    }

    fun getBatteryPhrase(percent: Int, isCharging: Boolean, grade: BatteryLevelGrade): String {
        if (isCharging) {
            return "ఫోన్ ఛార్జింగ్ అవుతోంది, బ్యాటరీ ${numberToTelugu(percent)} శాతం ఉంది."
        }
        return when (grade) {
            BatteryLevelGrade.EXCELLENT, BatteryLevelGrade.GOOD -> "బ్యాటరీ బాగుంది, ${numberToTelugu(percent)} శాతం ఉంది."
            BatteryLevelGrade.MEDIUM -> "బ్యాటరీ సగం ఉంది, ${numberToTelugu(percent)} శాతం ఉంది."
            BatteryLevelGrade.LOW -> "బ్యాటరీ తగ్గిపోయింది, దయచేసి ఛార్జర్ పెట్టండి."
            BatteryLevelGrade.CRITICAL -> "బ్యాటరీ చాలా తక్కువగా ఉంది, వెంటనే ఛార్జర్ పెట్టండి."
            BatteryLevelGrade.CHARGING -> "ఫోన్ ఛార్జింగ్ అవుతోంది."
        }
    }

    fun getSignalPhrase(grade: SignalGrade, isSimAvailable: Boolean): String {
        if (!isSimAvailable) return "ఫోన్‌లో సిమ్ కార్డు కనిపించడం లేదు."
        return when (grade) {
            SignalGrade.EXCELLENT, SignalGrade.GOOD -> "ఫోన్ సిగ్నల్ చాలా బాగుంది."
            SignalGrade.POOR -> "సిగ్నల్ తక్కువగా ఉంది."
            SignalGrade.NO_SIGNAL -> "సిగ్నల్ అందడం లేదు."
            SignalGrade.AIRPLANE_MODE -> "ఏరోప్లేన్ మోడ్ ఆన్‌లో ఉంది."
        }
    }

    fun getInternetPhrase(isInternetAvailable: Boolean): String {
        return if (isInternetAvailable) {
            "ఇంటర్నెట్ బాగా వస్తోంది."
        } else {
            "ఇంటర్నెట్ కనెక్షన్ లేదు."
        }
    }

    fun getTellMeStatusSummary(
        calendar: Calendar,
        batteryPercent: Int,
        isCharging: Boolean,
        batteryGrade: BatteryLevelGrade,
        signalGrade: SignalGrade,
        isSimAvailable: Boolean,
        isInternetAvailable: Boolean
    ): String {
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12Raw = calendar.get(Calendar.HOUR)
        val hour12 = if (hour12Raw == 0) 12 else hour12Raw
        val minute = calendar.get(Calendar.MINUTE)

        val timePeriod = when (hour24) {
            in 4..11 -> "ఉదయం"
            in 12..15 -> "మధ్యాహ్నం"
            in 16..19 -> "సాయంత్రం"
            else -> "రాత్రి"
        }

        val hourTelugu = numberToTelugu(hour12)
        val minuteTelugu = numberToTelugu(minute)

        val timeString = if (minute == 0) "$timePeriod $hourTelugu గంటలు" else "$timePeriod $hourTelugu గంటల $minuteTelugu నిమిషాలు"
        val batteryString = "బ్యాటరీ ${numberToTelugu(batteryPercent)} శాతం ఉంది"
        val signalString = if (isSimAvailable) "సిగ్నల్ బాగుంది" else "సిమ్ కార్డు లేదు"
        val netString = if (isInternetAvailable) "ఇంటర్నెట్ వస్తోంది" else "నెట్ లేదు"

        return "ఇప్పుడు $timeString. $batteryString, $signalString, మరియు $netString."
    }

    fun getCallingAnnouncement(contactName: String, transport: CallTransport, isEmergency: Boolean = false): String {
        val safeName = transliterateToTelugu(contactName)
        if (isEmergency) {
            return "సహాయం కోసం $safeName గారికి వెంటనే ఫోన్ కలుపుతున్నాను."
        }
        return when (transport) {
            CallTransport.CELLULAR -> "$safeName గారికి ఫోన్ కలుపుతున్నాను."
            CallTransport.WHATSAPP_AUDIO -> "$safeName గారికి వాట్సాప్ ఫోన్ కలుపుతున్నాను."
            CallTransport.WHATSAPP_VIDEO -> "$safeName గారికి వాట్సాప్ వీడియో కాల్ కలుపుతున్నాను."
        }
    }

    fun getFallbackPromptPhrase(contactName: String): String {
        val safeName = transliterateToTelugu(contactName)
        return "ఇంటర్నెట్ లేదు. $safeName గారికి సాధారణ ఫోన్ చేయమంటారా?"
    }

    fun getCallConnectedPhrase(): String = "ఫోన్ కలిసింది."

    fun getCallFailedPhrase(): String = "ఫోన్ కలవలేదు, దయచేసి మళ్లీ ప్రయత్నించండి."

    fun getWhatsAppNotInstalledPhrase(): String = "ఫోన్‌లో వాట్సాప్ లేదు."

    fun getEmergencyHoldingPhrase(contactName: String): String {
        val safeName = transliterateToTelugu(contactName)
        return "సహాయం కోసం $safeName గారికి ఫోన్ చేయడానికి బటన్‌ను పట్టుకోండి."
    }
}
