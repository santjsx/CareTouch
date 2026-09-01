package com.example.amma.telecom

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.amma.model.CallState
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.model.FailureReason
import com.example.amma.model.SystemStatus
import com.example.amma.voice.TeluguPhraseResolver

class CallOrchestrator(private val context: Context) {

    private val appContext = context.applicationContext
    private var lastCallTimestamp: Long = 0L

    fun initiateCall(
        contact: Contact,
        transport: CallTransport,
        status: SystemStatus,
        isEmergency: Boolean = false,
        onStateChange: (CallState) -> Unit
    ) {
        // Edge Case 1: Debounce rapid accidental double-taps
        val now = System.currentTimeMillis()
        if (now - lastCallTimestamp < 1200L) {
            Log.d(TAG, "Ignored duplicate call initiation within debounce window")
            return
        }
        lastCallTimestamp = now

        val cleanNumber = contact.phoneNumber.filter { it.isDigit() || it == '+' }
        val isEmergencyNumber = isEmergency || cleanNumber in listOf("112", "100", "108", "911", "101")
        val contactName = contact.effectivePronunciation

        when (transport) {
            CallTransport.CELLULAR -> {
                // Edge Case 2: Allow emergency numbers even if SIM reports false/unknown
                if (!status.isSimAvailable && !isEmergencyNumber) {
                    onStateChange(
                        CallState.CallFailed(
                            contact = contact,
                            reason = FailureReason.NO_CELLULAR_SIGNAL,
                            teluguMessage = "ఫోన్ సిమ్ అందుబాటులో లేదు."
                        )
                    )
                    return
                }

                // Place cellular call
                placeCellularCall(contact, isEmergencyNumber, onStateChange)
            }

            CallTransport.WHATSAPP_AUDIO, CallTransport.WHATSAPP_VIDEO -> {
                // Edge Case 3: Check Internet availability before attempting VoIP
                if (!status.isInternetAvailable) {
                    onStateChange(
                        CallState.FallbackPrompt(
                            contact = contact,
                            originalTransport = transport,
                            suggestedTransport = CallTransport.CELLULAR,
                            reasonMessageTelugu = TeluguPhraseResolver.getFallbackPromptPhrase(contactName)
                        )
                    )
                    return
                }

                // Edge Case 4: Check WhatsApp Installation
                if (!status.isWhatsAppInstalled) {
                    onStateChange(
                        CallState.FallbackPrompt(
                            contact = contact,
                            originalTransport = transport,
                            suggestedTransport = CallTransport.CELLULAR,
                            reasonMessageTelugu = "వాట్సాప్ లేదు. సాధారణ ఫోన్ చేయమంటారా?"
                        )
                    )
                    return
                }

                // Launch WhatsApp with auto-fallback to cellular on error
                launchWhatsAppCall(contact, transport, onStateChange)
            }
        }
    }

    private fun placeCellularCall(
        contact: Contact,
        isEmergency: Boolean,
        onStateChange: (CallState) -> Unit
    ) {
        val cleanNumber = contact.phoneNumber.filter { it.isDigit() || it == '+' }
        if (cleanNumber.isBlank()) {
            onStateChange(
                CallState.CallFailed(
                    contact = contact,
                    reason = FailureReason.INVALID_NUMBER,
                    teluguMessage = "ఫోన్ నంబర్ సరిగా లేదు."
                )
            )
            return
        }

        val hasCallPermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        // Edge Case 5: Use ACTION_CALL if granted, fallback safely to ACTION_DIAL
        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            onStateChange(CallState.Calling(contact, CallTransport.CELLULAR, isEmergency))
            appContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting cellular call activity", e)
            onStateChange(
                CallState.CallFailed(
                    contact = contact,
                    reason = FailureReason.UNKNOWN,
                    teluguMessage = "ఫోన్ చేయడం సాధ్యం కాలేదు."
                )
            )
        }
    }

    private fun launchWhatsAppCall(
        contact: Contact,
        transport: CallTransport,
        onStateChange: (CallState) -> Unit
    ) {
        var rawNumber = contact.whatsappNumber.filter { it.isDigit() }
        if (rawNumber.isBlank()) {
            rawNumber = contact.phoneNumber.filter { it.isDigit() }
        }

        // Edge Case 6: Standardize India 10-digit / 11-digit numbers for WhatsApp API
        if (rawNumber.length == 10) {
            rawNumber = "91$rawNumber"
        } else if (rawNumber.startsWith("0") && rawNumber.length == 11) {
            rawNumber = "91" + rawNumber.substring(1)
        }

        try {
            onStateChange(CallState.Calling(contact, transport))

            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$rawNumber")
            val pm = appContext.packageManager
            val hasRegularWa = try { pm.getPackageInfo("com.whatsapp", 0); true } catch(e: Exception) { false }
            val hasBusinessWa = try { pm.getPackageInfo("com.whatsapp.w4b", 0); true } catch(e: Exception) { false }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (hasRegularWa) {
                    setPackage("com.whatsapp")
                } else if (hasBusinessWa) {
                    setPackage("com.whatsapp.w4b")
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            appContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching WhatsApp intent, attempting package launcher fallback", e)
            try {
                val pm = appContext.packageManager
                val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
                    ?: pm.getLaunchIntentForPackage("com.whatsapp.w4b")

                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    appContext.startActivity(launchIntent)
                } else {
                    // Fallback to browser without package filter
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$rawNumber")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(webIntent)
                }
            } catch (e2: Exception) {
                // Edge Case 7: Seamless fallback prompt if WhatsApp launch completely fails
                onStateChange(
                    CallState.FallbackPrompt(
                        contact = contact,
                        originalTransport = transport,
                        suggestedTransport = CallTransport.CELLULAR,
                        reasonMessageTelugu = "వాట్సాప్ తెరవడం సాధ్యం కాలేదు. సాధారణ ఫోన్ చేయమంటారా?"
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "CallOrchestrator"
    }
}
