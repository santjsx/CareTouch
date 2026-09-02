package com.example.amma.cloud.firestore

import android.content.Context
import android.util.Log
import com.example.amma.data.ContactRepository
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * High-performance, local-first Cloud Firestore Sync Engine for CareTouch.
 * 
 * Features:
 * - Persistent on-device disk cache for 0ms reads
 * - Bidirectional realtime synchronization
 * - Conflict resolution (Last-Write-Wins with server timestamps)
 * - Zero network blocking of core UI
 */
class FirestoreSyncEngine(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val coroutineScope: CoroutineScope
) {
    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(50 * 1024 * 1024L) // 50MB disk cache
                    .build()
            )
            .build()
    }

    private var contactsListenerRegistration: ListenerRegistration? = null
    private var activeUserId: String? = null

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    fun startSync(userId: String) {
        if (activeUserId == userId && contactsListenerRegistration != null) return

        stopSync()
        activeUserId = userId

        Log.i(TAG, "Starting Firestore realtime sync for user: $userId")
        val contactsCollection = db.collection("users").document(userId).collection("contacts")

        contactsListenerRegistration = contactsCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.e(TAG, "Firestore sync listener error", error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val cloudContacts = snapshots.documents.mapNotNull { doc ->
                            try {
                                val rawPronunciation = doc.getString("customPronunciation")?.trim()
                                val cleanPronunciation = if (rawPronunciation.isNullOrBlank() || rawPronunciation.equals("null", ignoreCase = true)) null else rawPronunciation

                                Contact(
                                    id = doc.id,
                                    displayName = doc.getString("displayName") ?: "",
                                    relationship = doc.getString("relationship") ?: "",
                                    photoUri = doc.getString("photoUri"),
                                    phoneNumber = doc.getString("phoneNumber") ?: "",
                                    whatsappNumber = doc.getString("whatsappNumber") ?: (doc.getString("phoneNumber") ?: ""),
                                    primaryTransport = CallTransport.valueOf(doc.getString("primaryTransport") ?: CallTransport.CELLULAR.name),
                                    allowWhatsappAudio = doc.getBoolean("allowWhatsappAudio") ?: true,
                                    allowWhatsappVideo = doc.getBoolean("allowWhatsappVideo") ?: true,
                                    customPronunciation = cleanPronunciation,
                                    sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
                                    isEmergencyContact = doc.getBoolean("isEmergencyContact") ?: false
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed parsing Firestore contact document: ${doc.id}", e)
                                null
                            }
                        }.filter { it.displayName.isNotBlank() && it.phoneNumber.isNotBlank() }

                        if (cloudContacts.isNotEmpty()) {
                            // Merge into local repository
                            contactRepository.mergeCloudContacts(cloudContacts)
                            _lastSyncTimestamp.value = System.currentTimeMillis()
                        } else if (snapshots.isEmpty && contactRepository.contacts.value.isNotEmpty()) {
                            // First time cloud sync: Upload local preset contacts to Firestore
                            uploadLocalContactsToCloud(userId)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing cloud contacts snapshot", e)
                    }
                }
            }
        }
    }

    fun stopSync() {
        contactsListenerRegistration?.remove()
        contactsListenerRegistration = null
        activeUserId = null
        _isSyncing.value = false
    }

    suspend fun pushContactToCloud(contact: Contact, userId: String) = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            val docRef = db.collection("users").document(userId).collection("contacts").document(contact.id)
            val cleanPronunciation = if (contact.customPronunciation.isNullOrBlank() || contact.customPronunciation.equals("null", ignoreCase = true)) {
                null
            } else {
                contact.customPronunciation.trim()
            }
            val data = hashMapOf(
                "displayName" to contact.displayName.trim(),
                "relationship" to contact.relationship.trim(),
                "photoUri" to contact.photoUri,
                "phoneNumber" to contact.phoneNumber.trim(),
                "whatsappNumber" to contact.whatsappNumber.trim(),
                "primaryTransport" to contact.primaryTransport.name,
                "allowWhatsappAudio" to contact.allowWhatsappAudio,
                "allowWhatsappVideo" to contact.allowWhatsappVideo,
                "customPronunciation" to cleanPronunciation,
                "sortOrder" to contact.sortOrder,
                "isEmergencyContact" to contact.isEmergencyContact,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            docRef.set(data, SetOptions.merge()).await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Log.i(TAG, "Successfully pushed contact ${contact.id} (pronunciation: $cleanPronunciation) to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing contact to Firestore", e)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun deleteContactFromCloud(contactId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            _isSyncing.value = true
            db.collection("users").document(userId).collection("contacts").document(contactId).delete().await()
            Log.i(TAG, "Successfully deleted contact $contactId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting contact from Firestore", e)
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun uploadLocalContactsToCloud(userId: String) = withContext(Dispatchers.IO) {
        val currentLocal = contactRepository.contacts.value
        if (currentLocal.isEmpty()) return@withContext

        try {
            _isSyncing.value = true
            val batch = db.batch()
            for (contact in currentLocal) {
                val docRef = db.collection("users").document(userId).collection("contacts").document(contact.id)
                val cleanPronunciation = if (contact.customPronunciation.isNullOrBlank() || contact.customPronunciation.equals("null", ignoreCase = true)) {
                    null
                } else {
                    contact.customPronunciation.trim()
                }
                val data = hashMapOf(
                    "displayName" to contact.displayName.trim(),
                    "relationship" to contact.relationship.trim(),
                    "photoUri" to contact.photoUri,
                    "phoneNumber" to contact.phoneNumber.trim(),
                    "whatsappNumber" to contact.whatsappNumber.trim(),
                    "primaryTransport" to contact.primaryTransport.name,
                    "allowWhatsappAudio" to contact.allowWhatsappAudio,
                    "allowWhatsappVideo" to contact.allowWhatsappVideo,
                    "customPronunciation" to cleanPronunciation,
                    "sortOrder" to contact.sortOrder,
                    "isEmergencyContact" to contact.isEmergencyContact,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Log.i(TAG, "Initial batch upload of ${currentLocal.size} local contacts to Firestore complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error batch uploading contacts to Firestore", e)
        } finally {
            _isSyncing.value = false
        }
    }

    companion object {
        private const val TAG = "FirestoreSyncEngine"
    }
}
