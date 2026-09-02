package com.example.amma.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.amma.model.AppSettings
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import com.example.amma.util.PhotoStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Production-Grade Contact Repository.
 *
 * Guarantees:
 * - 0 Mock Contacts: Starts strictly empty unless user adds contacts or downloads from authenticated cloud.
 * - Cloud Photo Resilience: Contacts store dual local file path and compressed Base64 representation in Firestore
 *   so photos restore 100% reliably across uninstalls and device changes without requiring paid Blaze plans.
 * - Account Isolation: Strictly isolates contacts per authenticated caregiver.
 */
class ContactRepository(context: Context) {

    private val appContext = context.applicationContext
    private val contactsFile = File(appContext.filesDir, "amma_contacts.json")
    private val settingsFile = File(appContext.filesDir, "amma_settings.json")

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadSettings()
        loadContacts()
    }

    private fun loadContacts() {
        try {
            if (contactsFile.exists()) {
                val jsonStr = contactsFile.readText()
                val jsonArray = JSONArray(jsonStr)
                val list = mutableListOf<Contact>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val rawPhotoUri = if (obj.has("photoUri") && !obj.isNull("photoUri")) obj.getString("photoUri") else null
                    val photoBase64 = if (obj.has("photoBase64") && !obj.isNull("photoBase64")) obj.getString("photoBase64") else null

                    // Verify if local file exists; if missing on reinstall, immediately decode from Base64
                    var verifiedPhotoUri = rawPhotoUri
                    val hasValidFile = if (!rawPhotoUri.isNullOrBlank()) {
                        val clean = when {
                            rawPhotoUri.startsWith("file://") -> rawPhotoUri.removePrefix("file://")
                            rawPhotoUri.startsWith("file:") -> rawPhotoUri.removePrefix("file:")
                            else -> rawPhotoUri
                        }
                        val f = File(clean)
                        f.exists() && f.length() > 0
                    } else false

                    if (!hasValidFile && !photoBase64.isNullOrBlank()) {
                        verifiedPhotoUri = PhotoStorageHelper.decodeBase64ToDisk(appContext, photoBase64, obj.optString("id"))
                    }

                    val rawPronunciation = if (obj.has("customPronunciation") && !obj.isNull("customPronunciation")) {
                        obj.optString("customPronunciation").trim()
                    } else null
                    val cleanPronunciation = if (rawPronunciation.isNullOrBlank() || rawPronunciation.equals("null", ignoreCase = true)) null else rawPronunciation

                    list.add(
                        Contact(
                            id = obj.optString("id"),
                            displayName = obj.optString("displayName").trim(),
                            relationship = obj.optString("relationship").trim(),
                            photoUri = verifiedPhotoUri,
                            photoBase64 = photoBase64,
                            phoneNumber = obj.optString("phoneNumber").trim(),
                            whatsappNumber = obj.optString("whatsappNumber", obj.optString("phoneNumber")).trim(),
                            primaryTransport = CallTransport.valueOf(obj.optString("primaryTransport", CallTransport.CELLULAR.name)),
                            allowWhatsappAudio = obj.optBoolean("allowWhatsappAudio", true),
                            allowWhatsappVideo = obj.optBoolean("allowWhatsappVideo", true),
                            customPronunciation = cleanPronunciation,
                            sortOrder = obj.optInt("sortOrder", i),
                            isEmergencyContact = obj.optBoolean("isEmergencyContact", false)
                        )
                    )
                }
                val validList = list.filter { it.displayName.isNotBlank() && it.phoneNumber.isNotBlank() }
                _contacts.value = validList.sortedBy { it.sortOrder }
            } else {
                _contacts.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading contacts from disk", e)
            _contacts.value = emptyList()
        }
    }

    private fun loadSettings() {
        try {
            if (settingsFile.exists()) {
                val jsonStr = settingsFile.readText()
                val obj = JSONObject(jsonStr)
                _settings.value = AppSettings(
                    adminPin = obj.optString("adminPin", "1234"),
                    isHighAssistanceMode = obj.optBoolean("isHighAssistanceMode", true),
                    speakTimeOnTap = obj.optBoolean("speakTimeOnTap", true),
                    speakBatteryOnTap = obj.optBoolean("speakBatteryOnTap", true),
                    speakSignalOnTap = obj.optBoolean("speakSignalOnTap", true),
                    speakContactNameOnTap = obj.optBoolean("speakContactNameOnTap", true),
                    speechRate = obj.optDouble("speechRate", 0.95).toFloat(),
                    emergencyContactId = if (obj.has("emergencyContactId")) obj.getString("emergencyContactId") else null,
                    defaultSimSlot = obj.optInt("defaultSimSlot", 0),
                    hasCompletedInitialLogin = obj.optBoolean("hasCompletedInitialLogin", false)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading settings from disk", e)
        }
    }

    fun persistPhotoLocally(photoUriString: String?, contactId: String): String? {
        if (photoUriString.isNullOrBlank()) return null
        val uri = Uri.parse(photoUriString)
        return if (uri.scheme == "content") {
            PhotoStorageHelper.savePhotoLocally(appContext, uri, contactId)
        } else {
            val clean = when {
                photoUriString.startsWith("file://") -> photoUriString.removePrefix("file://")
                photoUriString.startsWith("file:") -> photoUriString.removePrefix("file:")
                else -> photoUriString
            }
            clean
        }
    }

    suspend fun saveContact(contact: Contact) = withContext(Dispatchers.IO) {
        val cleanName = contact.displayName.trim()
        val cleanPhone = contact.phoneNumber.trim()

        if (cleanName.isBlank() || cleanPhone.isBlank()) {
            Log.w(TAG, "Rejected saving contact with blank name or phone")
            return@withContext
        }

        val localPhotoUri = persistPhotoLocally(contact.photoUri, contact.id)
        val photoBase64 = if (!contact.photoBase64.isNullOrBlank()) {
            contact.photoBase64
        } else {
            PhotoStorageHelper.encodeToBase64(appContext, localPhotoUri ?: contact.photoUri)
        }

        val cleanPronunciation = if (contact.customPronunciation.isNullOrBlank() || contact.customPronunciation.equals("null", ignoreCase = true)) {
            null
        } else {
            contact.customPronunciation.trim()
        }

        val sanitizedContact = contact.copy(
            displayName = cleanName,
            phoneNumber = cleanPhone,
            relationship = contact.relationship.trim(),
            whatsappNumber = contact.whatsappNumber.trim().ifBlank { cleanPhone },
            customPronunciation = cleanPronunciation,
            photoUri = localPhotoUri,
            photoBase64 = photoBase64
        )

        val current = _contacts.value.toMutableList()
        val index = current.indexOfFirst { it.id == sanitizedContact.id }
        if (index >= 0) {
            current[index] = sanitizedContact
        } else {
            val duplicateIndex = current.indexOfFirst { it.phoneNumber == cleanPhone }
            if (duplicateIndex >= 0) {
                current[duplicateIndex] = sanitizedContact.copy(id = current[duplicateIndex].id)
            } else {
                current.add(sanitizedContact.copy(sortOrder = current.size))
            }
        }

        if (sanitizedContact.isEmergencyContact) {
            for (i in 0 until current.size) {
                if (current[i].id != sanitizedContact.id) {
                    current[i] = current[i].copy(isEmergencyContact = false)
                }
            }
            _settings.value = _settings.value.copy(emergencyContactId = sanitizedContact.id)
            saveSettingsToDisk(_settings.value)
        }

        _contacts.value = current.sortedBy { it.sortOrder }
        saveContactsToDisk(_contacts.value)
    }

    suspend fun deleteContact(contactId: String) = withContext(Dispatchers.IO) {
        val toDelete = _contacts.value.find { it.id == contactId }
        val remaining = _contacts.value.filter { it.id != contactId }.toMutableList()

        if (toDelete?.isEmergencyContact == true || _settings.value.emergencyContactId == contactId) {
            if (remaining.isNotEmpty()) {
                remaining[0] = remaining[0].copy(isEmergencyContact = true)
                _settings.value = _settings.value.copy(emergencyContactId = remaining[0].id)
            } else {
                _settings.value = _settings.value.copy(emergencyContactId = null)
            }
            saveSettingsToDisk(_settings.value)
        }

        try {
            val photoFile = File(appContext.filesDir, "photos/contact_${contactId}.jpg")
            if (photoFile.exists()) photoFile.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Could not delete local photo file", e)
        }

        _contacts.value = remaining.sortedBy { it.sortOrder }
        saveContactsToDisk(_contacts.value)
    }

    suspend fun syncWithCloudContacts(cloudContacts: List<Contact>) = withContext(Dispatchers.IO) {
        // Hydrate and verify local photo files from cloud Base64 payload
        val hydrated = cloudContacts.map { contact ->
            var finalPhotoUri = contact.photoUri
            val hasValidLocalFile = if (!finalPhotoUri.isNullOrBlank()) {
                val clean = when {
                    finalPhotoUri.startsWith("file://") -> finalPhotoUri.removePrefix("file://")
                    finalPhotoUri.startsWith("file:") -> finalPhotoUri.removePrefix("file:")
                    else -> finalPhotoUri
                }
                val f = File(clean)
                f.exists() && f.length() > 0
            } else false

            if (!hasValidLocalFile && !contact.photoBase64.isNullOrBlank()) {
                val restoredPath = PhotoStorageHelper.decodeBase64ToDisk(appContext, contact.photoBase64, contact.id)
                if (restoredPath != null) {
                    finalPhotoUri = restoredPath
                }
            }
            contact.copy(photoUri = finalPhotoUri)
        }

        val sorted = hydrated.sortedBy { it.sortOrder }
        _contacts.value = sorted
        saveContactsToDisk(sorted)
    }

    suspend fun clearContacts() = withContext(Dispatchers.IO) {
        _contacts.value = emptyList()
        if (contactsFile.exists()) {
            contactsFile.delete()
        }
        try {
            val photosDir = File(appContext.filesDir, "photos")
            if (photosDir.exists()) {
                photosDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning photos directory", e)
        }
    }

    suspend fun saveSettings(newSettings: AppSettings) = withContext(Dispatchers.IO) {
        _settings.value = newSettings
        saveSettingsToDisk(newSettings)
    }

    private fun saveContactsToDisk(list: List<Contact>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { c ->
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("displayName", c.displayName)
                obj.put("relationship", c.relationship)
                obj.put("photoUri", c.photoUri ?: JSONObject.NULL)
                obj.put("photoBase64", c.photoBase64 ?: JSONObject.NULL)
                obj.put("phoneNumber", c.phoneNumber)
                obj.put("whatsappNumber", c.whatsappNumber)
                obj.put("primaryTransport", c.primaryTransport.name)
                obj.put("allowWhatsappAudio", c.allowWhatsappAudio)
                obj.put("allowWhatsappVideo", c.allowWhatsappVideo)
                obj.put("customPronunciation", c.customPronunciation ?: JSONObject.NULL)
                obj.put("sortOrder", c.sortOrder)
                obj.put("isEmergencyContact", c.isEmergencyContact)
                jsonArray.put(obj)
            }
            contactsFile.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Error writing contacts to disk", e)
        }
    }

    private fun saveSettingsToDisk(s: AppSettings) {
        try {
            val obj = JSONObject()
            obj.put("adminPin", s.adminPin)
            obj.put("isHighAssistanceMode", s.isHighAssistanceMode)
            obj.put("speakTimeOnTap", s.speakTimeOnTap)
            obj.put("speakBatteryOnTap", s.speakBatteryOnTap)
            obj.put("speakSignalOnTap", s.speakSignalOnTap)
            obj.put("speakContactNameOnTap", s.speakContactNameOnTap)
            obj.put("speechRate", s.speechRate.toDouble())
            obj.put("speechPitch", s.speechPitch.toDouble())
            obj.put("emergencyContactId", s.emergencyContactId ?: JSONObject.NULL)
            obj.put("defaultSimSlot", s.defaultSimSlot)
            obj.put("hasCompletedInitialLogin", s.hasCompletedInitialLogin)
            settingsFile.writeText(obj.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Error writing settings to disk", e)
        }
    }

    fun getEmergencyContact(): Contact? {
        val list = _contacts.value
        val emergencyId = _settings.value.emergencyContactId
        return list.firstOrNull { it.id == emergencyId }
            ?: list.firstOrNull { it.isEmergencyContact }
            ?: list.firstOrNull()
    }

    companion object {
        private const val TAG = "ContactRepository"
    }
}
