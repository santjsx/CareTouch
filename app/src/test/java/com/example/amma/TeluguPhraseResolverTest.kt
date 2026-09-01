package com.example.amma

import com.example.amma.model.BatteryLevelGrade
import com.example.amma.model.CallTransport
import com.example.amma.model.SignalGrade
import com.example.amma.voice.TeluguPhraseResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TeluguPhraseResolverTest {

    @Test
    fun numberToTelugu_convertsCorrectly() {
        assertEquals("ఒకటి", TeluguPhraseResolver.numberToTelugu(1))
        assertEquals("పది", TeluguPhraseResolver.numberToTelugu(10))
        assertEquals("నలభై రెండు", TeluguPhraseResolver.numberToTelugu(42))
        assertEquals("ఎనభై", TeluguPhraseResolver.numberToTelugu(80))
        assertEquals("వంద", TeluguPhraseResolver.numberToTelugu(100))
    }

    @Test
    fun getTimePhrase_formatsMorningTime() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 42)
        }
        val phrase = TeluguPhraseResolver.getTimePhrase(cal)
        assertEquals("ఇప్పుడు ఉదయం పది గంటల నలభై రెండు నిమిషాలు.", phrase)
    }

    @Test
    fun getTimePhrase_formatsNightTime() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 15)
        }
        val phrase = TeluguPhraseResolver.getTimePhrase(cal)
        assertEquals("ఇప్పుడు రాత్రి తొమ్మిది గంటల పదిహేను నిమిషాలు.", phrase)
    }

    @Test
    fun getBatteryPhrase_chargingState() {
        val phrase = TeluguPhraseResolver.getBatteryPhrase(82, isCharging = true, BatteryLevelGrade.CHARGING)
        assertTrue(phrase.contains("ఛార్జ్ అవుతోంది"))
        assertTrue(phrase.contains("ఎనభై రెండు"))
    }

    @Test
    fun getBatteryPhrase_lowWarningState() {
        val phrase = TeluguPhraseResolver.getBatteryPhrase(12, isCharging = false, BatteryLevelGrade.LOW)
        assertTrue(phrase.contains("బ్యాటరీ తక్కువగా ఉంది"))
    }

    @Test
    fun getCallingAnnouncement_cellular() {
        val announcement = TeluguPhraseResolver.getCallingAnnouncement("సంతోష్", CallTransport.CELLULAR)
        assertEquals("సంతోష్ కి ఫోన్ చేస్తున్నాను.", announcement)
    }

    @Test
    fun getCallingAnnouncement_whatsAppVideo() {
        val announcement = TeluguPhraseResolver.getCallingAnnouncement("స్వప్న", CallTransport.WHATSAPP_VIDEO)
        assertEquals("స్వప్న కి వాట్సాప్ వీడియో కాల్ చేస్తున్నాను.", announcement)
    }

    @Test
    fun getCallingAnnouncement_emergency() {
        val announcement = TeluguPhraseResolver.getCallingAnnouncement("సంతోష్", CallTransport.CELLULAR, isEmergency = true)
        assertEquals("సహాయం కోసం సంతోష్ కి ఫోన్ చేస్తున్నాను.", announcement)
    }

    @Test
    fun getFallbackPromptPhrase_matchesContract() {
        val prompt = TeluguPhraseResolver.getFallbackPromptPhrase("సంతోష్")
        assertEquals("ఇంటర్నెట్ లేదు. సంతోష్ కి సాధారణ ఫోన్ చేయాలా?", prompt)
    }
}
