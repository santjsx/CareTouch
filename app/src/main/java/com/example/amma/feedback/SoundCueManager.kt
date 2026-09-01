package com.example.amma.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

class SoundCueManager {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundCueManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playCallTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        } catch (e: Exception) {
            Log.e("SoundCueManager", "Error playing call tone", e)
        }
    }

    fun playErrorTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 300)
        } catch (e: Exception) {
            Log.e("SoundCueManager", "Error playing error tone", e)
        }
    }

    fun playTapTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (e: Exception) {
            Log.e("SoundCueManager", "Error playing tap tone", e)
        }
    }

    fun playEmergencyAlertTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
        } catch (e: Exception) {
            Log.e("SoundCueManager", "Error playing emergency tone", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
