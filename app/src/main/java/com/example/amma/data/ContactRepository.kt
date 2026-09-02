package com.example.amma.data

import android.content.Context
import android.util.Log
import com.example.amma.model.AppSettings
import com.example.amma.model.CallTransport
import com.example.amma.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
                    val verifiedPhotoUri = if (rawPhotoUri != null && rawPhotoUri.startsWith("file:")) {
                        try {
                            val file = File(java.net.URI.create(rawPhotoUri))
                            if (file.exists() && file.length() > 0) rawPhotoUri else null
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        rawPhotoUri
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
                _contacts.value = (if (validList.isNotEmpty()) validList else getInitialPresets()).sortedBy { it.sortOrder }
                saveContactsToDisk(_contacts.value)
            } else {
                // Populate realistic initial family presets
                _contacts.value = getInitialPresets()
                saveContactsToDisk(_contacts.value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading contacts from disk", e)
            _contacts.value = getInitialPresets()
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
        if (photoUriString.startsWith("file:") && photoUriString.contains("photos/contact_")) {
            return photoUriString // Already persisted in private storage
        }

        return try {
            val uri = android.net.Uri.parse(photoUriString)
            if (uri.scheme == "content") {
                try {
                    appContext.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Not all content URIs support persistable permissions
                }
            }

            val photosDir = File(appContext.filesDir, "photos").apply { if (!exists()) mkdirs() }
            val destFile = File(photosDir, "contact_${contactId}.jpg")

            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (originalBitmap != null) {
                    val maxDim = 512
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > maxDim || height > maxDim) {
                        maxDim.toFloat() / maxOf(width, height)
                    } else 1.0f

                    val scaledBitmap = if (scale < 1.0f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            originalBitmap,
                            (width * scale).toInt().coerceAtLeast(1),
                            (height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else originalBitmap

                    java.io.FileOutputStream(destFile).use { out ->
                        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, out)
                    }
                    if (scaledBitmap != originalBitmap) {
                        originalBitmap.recycle()
                    }
                    destFile.toURI().toString()
                } else {
                    photoUriString
                }
            } ?: photoUriString
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting photo locally for contact $contactId", e)
            photoUriString
        }
    }

    suspend fun saveContact(contact: Contact) = withContext(Dispatchers.IO) {
        val cleanName = contact.displayName.trim()
        val cleanPhone = contact.phoneNumber.trim()

        // Edge Case 1: Refuse to save empty/blank contacts
        if (cleanName.isBlank() || cleanPhone.isBlank()) {
            Log.w(TAG, "Rejected saving contact with blank name or phone")
            return@withContext
        }

        // Edge Case 2: Persist photo to internal app storage so URI permissions never expire across reboots
        val localPhotoUri = persistPhotoLocally(contact.photoUri, contact.id)

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
            photoUri = localPhotoUri
        )

        val current = _contacts.value.toMutableList()
        val index = current.indexOfFirst { it.id == sanitizedContact.id }
        if (index >= 0) {
            current[index] = sanitizedContact
        } else {
            // Edge Case 3: Duplicate phone number prevention
            val duplicateIndex = current.indexOfFirst { it.phoneNumber == cleanPhone }
            if (duplicateIndex >= 0) {
                current[duplicateIndex] = sanitizedContact.copy(id = current[duplicateIndex].id)
            } else {
                current.add(sanitizedContact.copy(sortOrder = current.size))
            }
        }

        // If this contact is marked as emergency, clear other emergency flags
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

        // Edge Case 4: If deleting emergency contact, safely auto-reassign to first remaining contact
        if (toDelete?.isEmergencyContact == true || _settings.value.emergencyContactId == contactId) {
            if (remaining.isNotEmpty()) {
                remaining[0] = remaining[0].copy(isEmergencyContact = true)
                _settings.value = _settings.value.copy(emergencyContactId = remaining[0].id)
            } else {
                _settings.value = _settings.value.copy(emergencyContactId = null)
            }
            saveSettingsToDisk(_settings.value)
        }

        // Clean up local photo file
        try {
            val photoFile = File(appContext.filesDir, "photos/contact_${contactId}.jpg")
            if (photoFile.exists()) photoFile.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Could not delete local photo file", e)
        }

        _contacts.value = remaining.sortedBy { it.sortOrder }
        saveContactsToDisk(_contacts.value)
    }

    suspend fun mergeCloudContacts(cloudContacts: List<Contact>) = withContext(Dispatchers.IO) {
        if (cloudContacts.isEmpty()) return@withContext

        val current = _contacts.value.toMutableList()
        for (cloudContact in cloudContacts) {
            val index = current.indexOfFirst { it.id == cloudContact.id }
            if (index >= 0) {
                current[index] = cloudContact
            } else {
                current.add(cloudContact)
            }
        }

        val sorted = current.sortedBy { it.sortOrder }
        _contacts.value = sorted
        saveContactsToDisk(sorted)
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

    private fun getInitialPresets(): List<Contact> {
        return listOf(
            Contact(
                id = "preset_son",
                displayName = "Santhosh",
                relationship = "Son (కొడుకు)",
                phoneNumber = "9876543210",
                whatsappNumber = "9876543210",
                primaryTransport = CallTransport.CELLULAR,
                customPronunciation = "సంతోష్",
                sortOrder = 0,
                isEmergencyContact = true
            ),
            Contact(
                id = "preset_daughter",
                displayName = "Swapna",
                relationship = "Daughter (కూతురు)",
                phoneNumber = "9876543211",
                whatsappNumber = "9876543211",
                primaryTransport = CallTransport.WHATSAPP_VIDEO,
                customPronunciation = "స్వప్న",
                sortOrder = 1,
                isEmergencyContact = false
            ),
            Contact(
                id = "preset_husband",
                displayName = "Ramesh",
                relationship = "Husband (భర్త)",
                phoneNumber = "9876543212",
                whatsappNumber = "9876543212",
                primaryTransport = CallTransport.CELLULAR,
                customPronunciation = "రమేష్",
                sortOrder = 2,
                isEmergencyContact = false
            ),
            Contact(
                id = "preset_brother",
                displayName = "Srinu",
                relationship = "Brother (తమ్ముడు)",
                phoneNumber = "9876543213",
                whatsappNumber = "9876543213",
                primaryTransport = CallTransport.CELLULAR,
                customPronunciation = "శ్రీను",
                sortOrder = 3,
                isEmergencyContact = false
            ),
            Contact(
                id = "preset_doctor",
                displayName = "Doctor",
                relationship = "Doctor (వైద్యుడు)",
                phoneNumber = "9876543214",
                whatsappNumber = "9876543214",
                primaryTransport = CallTransport.CELLULAR,
                customPronunciation = "డాక్టర్",
                sortOrder = 4,
                isEmergencyContact = false
            )
        )
    }

    companion object {
        private const val TAG = "ContactRepository"
    }
}
