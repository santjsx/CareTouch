package com.example.amma.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amma.AmmaApplication
import com.example.amma.cloud.auth.AuthState
import com.example.amma.cloud.r2.R2Config
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
    val isAddContactOpen: Boolean = false,
    val authState: AuthState = AuthState.Unauthenticated,
    val r2Config: R2Config = R2Config(),
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long? = null
)

class AdminViewModel : ViewModel() {

    private val app = AmmaApplication.instance
    private val contactRepo = app.contactRepository
    private val voiceEngine = app.voiceGuidanceEngine
    private val statusEngine = app.systemStatusEngine
    private val authRepo = app.authRepository
    private val firestoreSync = app.firestoreSyncEngine
    private val r2Manager = app.r2StorageManager
    private val googleDriveManager = app.googleDriveStorageManager
    private val firebaseStorageManager = app.firebaseStorageManager

    private val _editingContact = MutableStateFlow<Contact?>(null)
    private val _isAddContactOpen = MutableStateFlow(false)

    val uiState: StateFlow<AdminUiState> = combine(
        combine(contactRepo.contacts, contactRepo.settings, statusEngine.status) { contacts, settings, status ->
            Triple(contacts, settings, status)
        },
        combine(voiceEngine.isTtsReady, _editingContact, _isAddContactOpen) { ttsReady, editing, isAddOpen ->
            Triple(ttsReady, editing, isAddOpen)
        },
        combine(authRepo.authState, r2Manager.config, firestoreSync.isSyncing, firestoreSync.lastSyncTimestamp) { auth, r2, syncing, lastSync ->
            Tuple4(auth, r2, syncing, lastSync)
        }
    ) { (contacts, settings, status), (ttsReady, editing, isAddOpen), (auth, r2, syncing, lastSync) ->
        AdminUiState(
            contacts = contacts,
            settings = settings,
            status = status,
            isTtsReady = ttsReady,
            editingContact = editing,
            isAddContactOpen = isAddOpen,
            authState = auth,
            r2Config = r2,
            isSyncing = syncing,
            lastSyncTimestamp = lastSync
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
            // 1. Save locally with immediate 0ms UI update
            contactRepo.saveContact(contact)
            closeContactDialog()

            // 2. If authenticated, upload photo and sync contact to Firestore
            val currentUser = authRepo.currentUserId
            if (currentUser != null) {
                var finalContact = contactRepo.contacts.value.find { it.id == contact.id } ?: contact
                val photoUri = finalContact.photoUri

                if (!photoUri.isNullOrBlank() && !photoUri.startsWith("http")) {
                    // Upload to caregiver's personal Google Drive (100% Free, uses 15GB personal quota, 0 Blaze plan needed)
                    // with fallback to Cloudflare R2 if configured
                    val cloudUrl = googleDriveManager.uploadContactPhoto(photoUri, contact.id)
                        ?: r2Manager.uploadContactPhoto(photoUri, contact.id, currentUser)

                    if (cloudUrl != null) {
                        finalContact = finalContact.copy(photoUri = cloudUrl)
                        contactRepo.saveContact(finalContact)
                    }
                }

                firestoreSync.pushContactToCloud(finalContact, currentUser)
            }
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            contactRepo.deleteContact(contactId)
            val currentUser = authRepo.currentUserId
            if (currentUser != null) {
                firestoreSync.deleteContactFromCloud(contactId, currentUser)
            }
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

    fun signInWithGoogle(context: Context, serverClientId: String = "") {
        viewModelScope.launch {
            authRepo.signInWithGoogle(context, serverClientId)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
        }
    }

    fun saveR2Config(r2Config: R2Config) {
        viewModelScope.launch {
            r2Manager.saveConfig(r2Config)
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
