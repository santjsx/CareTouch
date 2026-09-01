package com.example.amma.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amma.AmmaApplication
import com.example.amma.model.AppSettings
import com.example.amma.model.CallState
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.model.SystemStatus
import com.example.amma.voice.TeluguPhraseResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.amma.cloud.auth.AuthState

data class HomeUiState(
    val contacts: List<Contact> = emptyList(),
    val status: SystemStatus = SystemStatus(),
    val settings: AppSettings = AppSettings(),
    val emergencyContact: Contact? = null,
    val callState: CallState = CallState.Idle,
    val isSpeaking: Boolean = false,
    val showAdminAuth: Boolean = false,
    val authState: AuthState = AuthState.Unauthenticated,
    val showInitialLogin: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val app = AmmaApplication.instance
    private val contactRepo = app.contactRepository
    private val voiceEngine = app.voiceGuidanceEngine
    private val statusEngine = app.systemStatusEngine
    private val callOrchestrator = app.callOrchestrator
    private val haptics = app.hapticsManager
    private val soundCues = app.soundCueManager
    private val authRepo = app.authRepository

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    private val _showAdminAuth = MutableStateFlow(false)
    private val _dismissedInitialLogin = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(contactRepo.contacts, statusEngine.status, contactRepo.settings) { contacts, status, settings ->
            Triple(contacts, status, settings)
        },
        combine(_callState, voiceEngine.isSpeaking, _showAdminAuth) { callState, isSpeaking, showAdmin ->
            Triple(callState, isSpeaking, showAdmin)
        },
        authRepo.authState,
        _dismissedInitialLogin
    ) { (contacts, status, settings), (callState, isSpeaking, showAdmin), authState, dismissed ->
        val showInitial = !settings.hasCompletedInitialLogin && !dismissed && authState !is AuthState.Authenticated
        HomeUiState(
            contacts = contacts,
            status = status,
            settings = settings,
            emergencyContact = contactRepo.getEmergencyContact(),
            callState = callState,
            isSpeaking = isSpeaking,
            showAdminAuth = showAdmin,
            authState = authState,
            showInitialLogin = showInitial
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

    fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            val result = authRepo.signInWithGoogle(context)
            if (result.isSuccess) {
                val updated = contactRepo.settings.value.copy(hasCompletedInitialLogin = true)
                contactRepo.saveSettings(updated)
            }
        }
    }

    fun dismissInitialLogin() {
        _dismissedInitialLogin.value = true
        viewModelScope.launch {
            val updated = contactRepo.settings.value.copy(hasCompletedInitialLogin = true)
            contactRepo.saveSettings(updated)
        }
    }

    fun onContactTap(contact: Contact) {
        haptics.callInitiated()
        soundCues.playCallTone()

        val announcement = TeluguPhraseResolver.getCallingAnnouncement(
            contactName = contact.effectivePronunciation,
            transport = contact.primaryTransport
        )
        voiceEngine.speak(announcement)

        callOrchestrator.initiateCall(
            contact = contact,
            transport = contact.primaryTransport,
            status = uiState.value.status,
            onStateChange = { newState ->
                _callState.value = newState
            }
        )
    }

    fun onContactLongClick(contact: Contact) {
        haptics.tap()
        soundCues.playTapTone()
        _callState.value = CallState.ContactOptionsPicker(contact)
        voiceEngine.speak("${contact.effectivePronunciation} కి ఏ విధంగా ఫోన్ చేయాలి?")
    }

    fun onSelectTransport(contact: Contact, transport: CallTransport) {
        haptics.callInitiated()
        soundCues.playCallTone()

        val announcement = TeluguPhraseResolver.getCallingAnnouncement(
            contactName = contact.effectivePronunciation,
            transport = transport
        )
        voiceEngine.speak(announcement)

        callOrchestrator.initiateCall(
            contact = contact,
            transport = transport,
            status = uiState.value.status,
            onStateChange = { newState ->
                _callState.value = newState
            }
        )
    }

    private var lastStatusTapTime = 0L

    private fun shouldDebounceTap(windowMs: Long = 500L): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastStatusTapTime < windowMs) return true
        lastStatusTapTime = now
        return false
    }

    fun onClockTap() {
        if (shouldDebounceTap()) return
        haptics.tap()
        soundCues.playTapTone()
        val dateTimePhrase = TeluguPhraseResolver.getFullDateTimePhrase(Calendar.getInstance())
        voiceEngine.speak(dateTimePhrase)
    }

    fun onClockLongClick() {
        if (shouldDebounceTap()) return
        haptics.tap()
        val datePhrase = TeluguPhraseResolver.getDatePhrase(Calendar.getInstance())
        voiceEngine.speak(datePhrase)
    }

    fun onBatteryTap() {
        if (shouldDebounceTap()) return
        haptics.tap()
        val s = uiState.value.status
        val phrase = TeluguPhraseResolver.getBatteryPhrase(s.batteryPercent, s.isCharging, s.batteryGrade)
        voiceEngine.speak(phrase)
    }

    fun onSignalTap() {
        if (shouldDebounceTap()) return
        haptics.tap()
        val s = uiState.value.status
        val phrase = TeluguPhraseResolver.getSignalPhrase(s.signalGrade, s.isSimAvailable)
        voiceEngine.speak(phrase)
    }

    fun onInternetTap() {
        if (shouldDebounceTap()) return
        haptics.tap()
        val s = uiState.value.status
        val phrase = TeluguPhraseResolver.getInternetPhrase(s.isInternetAvailable)
        voiceEngine.speak(phrase)
    }

    fun onTellMeTap() {
        haptics.callInitiated()
        soundCues.playTapTone()
        val s = uiState.value.status
        val summary = TeluguPhraseResolver.getTellMeStatusSummary(
            calendar = Calendar.getInstance(),
            batteryPercent = s.batteryPercent,
            isCharging = s.isCharging,
            batteryGrade = s.batteryGrade,
            signalGrade = s.signalGrade,
            isSimAvailable = s.isSimAvailable,
            isInternetAvailable = s.isInternetAvailable
        )
        voiceEngine.speak(summary)
    }

    fun onEmergencyHoldTick() {
        haptics.emergencyHoldTick()
    }

    fun onEmergencyTriggered(contact: Contact) {
        haptics.emergencyTriggered()
        soundCues.playEmergencyAlertTone()

        val announcement = TeluguPhraseResolver.getCallingAnnouncement(
            contactName = contact.effectivePronunciation,
            transport = CallTransport.CELLULAR,
            isEmergency = true
        )
        voiceEngine.speak(announcement)

        callOrchestrator.initiateCall(
            contact = contact,
            transport = CallTransport.CELLULAR,
            status = uiState.value.status,
            isEmergency = true,
            onStateChange = { newState ->
                _callState.value = newState
            }
        )
    }

    fun onConfirmFallback(contact: Contact) {
        haptics.callInitiated()
        soundCues.playCallTone()
        voiceEngine.speak("${contact.effectivePronunciation} కి సాధారణ ఫోన్ చేస్తున్నాను.")
        callOrchestrator.initiateCall(
            contact = contact,
            transport = CallTransport.CELLULAR,
            status = uiState.value.status,
            onStateChange = { newState ->
                _callState.value = newState
            }
        )
    }

    fun dismissCallState() {
        _callState.value = CallState.Idle
    }

    fun openAdminAuth() {
        haptics.tap()
        _showAdminAuth.value = true
    }

    fun dismissAdminAuth() {
        _showAdminAuth.value = false
    }
}
