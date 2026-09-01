package com.example.amma.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amma.AmmaApplication
import com.example.amma.model.AppSettings
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.model.SystemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminUiState(
    val contacts: List<Contact> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val status: SystemStatus = SystemStatus(),
    val isTtsReady: Boolean = false,
    val editingContact: Contact? = null,
    val isAddContactOpen: Boolean = false
)

class AdminViewModel : ViewModel() {

    private val app = AmmaApplication.instance
    private val contactRepo = app.contactRepository
    private val voiceEngine = app.voiceGuidanceEngine
    private val statusEngine = app.systemStatusEngine

    private val _editingContact = MutableStateFlow<Contact?>(null)
    private val _isAddContactOpen = MutableStateFlow(false)

    val uiState: StateFlow<AdminUiState> = combine(
        combine(contactRepo.contacts, contactRepo.settings, statusEngine.status) { contacts, settings, status ->
            Triple(contacts, settings, status)
        },
        voiceEngine.isTtsReady,
        _editingContact,
        _isAddContactOpen
    ) { (contacts, settings, status), ttsReady, editing, isAddOpen ->
        AdminUiState(
            contacts = contacts,
            settings = settings,
            status = status,
            isTtsReady = ttsReady,
            editingContact = editing,
            isAddContactOpen = isAddOpen
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdminUiState()
    )

    fun openAddContact() {
        _editingContact.value = Contact(
            displayName = "",
            relationship = "",
            phoneNumber = "",
            whatsappNumber = "",
            primaryTransport = CallTransport.CELLULAR,
            customPronunciation = ""
        )
        _isAddContactOpen.value = true
    }

    fun openEditContact(contact: Contact) {
        _editingContact.value = contact
        _isAddContactOpen.value = true
    }

    fun closeContactDialog() {
        _editingContact.value = null
        _isAddContactOpen.value = false
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            contactRepo.saveContact(contact)
            closeContactDialog()
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            contactRepo.deleteContact(contactId)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            contactRepo.saveSettings(newSettings)
            voiceEngine.updateSpeechParameters(newSettings.speechRate, newSettings.speechPitch)
            voiceEngine.setNeuralVoice(newSettings.neuralVoice)
        }
    }

    fun testTeluguSpeech(phrase: String) {
        voiceEngine.speak(phrase)
    }
}
