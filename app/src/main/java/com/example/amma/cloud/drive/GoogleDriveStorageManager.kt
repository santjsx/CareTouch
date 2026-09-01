package com.example.amma.cloud.drive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Enterprise Google Drive Storage Manager for CareTouch Contact Photos.
 * 
 * Features:
 * - 100% Free: Uses caregiver's personal 15GB Google Drive quota (no credit card or Blaze plan needed)
 * - Automatic folder management: Creates and maintains a dedicated `CareTouch_Photos` folder on Google Drive
 * - Direct Google CDN Thumbnails (`https://lh3.googleusercontent.com/d/{fileId}`) for high-speed, cacheable loading
 * - On-device 512x512 JPEG compression (~35-60 KB)
 */
class GoogleDriveStorageManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val firebaseAuth = FirebaseAuth.getInstance()

    @Volatile
    private var cachedFolderId: String? = null

    /**
     * Obtains OAuth 2.0 Access Token for Google Drive using current Google Account.
     */
    private suspend fun getDriveAccessToken(): String? = withContext(Dispatchers.IO) {
        val email = firebaseAuth.currentUser?.email ?: return@withContext null
        try {
            val scope = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
            GoogleAuthUtil.getToken(appContext, email, scope)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Google Drive OAuth access token for: $email", e)
            null
        }
    }

    /**
     * Resolves or creates the `CareTouch_Photos` folder in the caregiver's Google Drive.
     */
    private suspend fun getOrCreateCareTouchFolder(token: String): String? = withContext(Dispatchers.IO) {
        cachedFolderId?.let { return@withContext it }

        try {
            // 1. Search for existing folder
            val searchUrl = "https://www.googleapis.com/drive/v3/files?q=name%3D'CareTouch_Photos'+and+mimeType%3D'application%2Fvnd.google-apps.folder'+and+trashed%3Dfalse&fields=files(id,name)"
            val searchRequest = Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            httpClient.newCall(searchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val files = json.optJSONArray("files")
                        if (files != null && files.length() > 0) {
                            val folderId = files.getJSONObject(0).getString("id")
                            cachedFolderId = folderId
                            Log.i(TAG, "Found existing CareTouch_Photos folder: $folderId")
                            return@withContext folderId
                        }
                    }
                }
            }

            // 2. Create folder if not found
            val createJson = JSONObject().apply {
                put("name", "CareTouch_Photos")
                put("mimeType", "application/vnd.google-apps.folder")
                put("description", "CareTouch Elder Accessibility Contact Photos")
            }

            val createRequest = Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .post(createJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .build()

            httpClient.newCall(createRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val folderId = json.getString("id")
                        cachedFolderId = folderId
                        Log.i(TAG, "Created new CareTouch_Photos folder: $folderId")
                        return@withContext folderId
                    }
                } else {
                    Log.e(TAG, "Error creating Drive folder: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating/finding Drive folder", e)
        }

        null
    }

    /**
     * Compresses and uploads a local contact photo to the caregiver's Google Drive.
     * 
     * Returns Google's high-speed CDN URL (`https://lh3.googleusercontent.com/d/{fileId}`)
     * or null if offline or upload fails.
     */
    suspend fun uploadContactPhoto(
        localPhotoUriString: String,
        contactId: String
    ): String? = withContext(Dispatchers.IO) {
        val token = getDriveAccessToken() ?: run {
            Log.d(TAG, "No Google Drive access token available, keeping local photo")
            return@withContext null
        }

        try {
            val uri = Uri.parse(localPhotoUriString)
            val inputStream = appContext.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            inputStream.close()

            // Resize to 512x512
            val maxDim = maxOf(originalBitmap.width, originalBitmap.height)
            val scale = if (maxDim > 512) 512f / maxDim else 1.0f
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scale).toInt().coerceAtLeast(1),
                (originalBitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()

            val folderId = getOrCreateCareTouchFolder(token)

            // Metadata JSON
            val metadataJson = JSONObject().apply {
                put("name", "contact_${contactId}_${System.currentTimeMillis()}.jpg")
                put("mimeType", "image/jpeg")
                if (folderId != null) {
                    put("parents", org.json.JSONArray().apply { put(folderId) })
                }
            }

            // Multipart Upload to Google Drive API
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "metadata",
                    "metadata",
                    metadataJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
                .addFormDataPart(
                    "file",
                    "photo.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val uploadRequest = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,webContentLink,thumbnailLink")
                .addHeader("Authorization", "Bearer $token")
                .post(multipartBody)
                .build()

            var uploadedFileId: String? = null
            httpClient.newCall(uploadRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        uploadedFileId = json.optString("id")
                    }
                } else {
                    Log.e(TAG, "Drive upload failed: HTTP ${response.code} ${response.message}")
                }
            }

            val fileId = uploadedFileId ?: return@withContext null
            Log.i(TAG, "Successfully uploaded photo to Google Drive: $fileId")

            // Enable public reader permission so elder phone loads photo seamlessly via Google CDN
            try {
                val permJson = JSONObject().apply {
                    put("role", "reader")
                    put("type", "anyone")
                }
                val permRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId/permissions")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .post(permJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    .build()

                httpClient.newCall(permRequest).execute().close()
            } catch (e: Exception) {
                Log.w(TAG, "Non-fatal error setting file permission", e)
            }

            // High-speed Google CDN link
            "https://lh3.googleusercontent.com/d/$fileId"
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo to Google Drive", e)
            null
        }
    }

    companion object {
        private const val TAG = "GoogleDriveStorage"
    }
}
