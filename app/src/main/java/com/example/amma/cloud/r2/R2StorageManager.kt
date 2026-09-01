package com.example.amma.cloud.r2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * High-performance, lightweight Cloudflare R2 Object Storage Manager.
 * 
 * Features:
 * - 100% S3-compatible AWS SigV4 authorization
 * - On-device 512x512 JPEG compression (~40-60KB)
 * - Zero external SDK overhead (Uses existing OkHttpClient)
 * - Automatic public CDN URL generation
 */
class R2StorageManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val httpClient = OkHttpClient.Builder().build()
    private val configFile = File(appContext.filesDir, "r2_config.json")

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<R2Config> = _config.asStateFlow()

    private fun loadConfig(): R2Config {
        return try {
            if (configFile.exists()) {
                val json = JSONObject(configFile.readText())
                R2Config(
                    accountId = json.optString("accountId"),
                    accessKeyId = json.optString("accessKeyId"),
                    secretAccessKey = json.optString("secretAccessKey"),
                    bucketName = json.optString("bucketName", "caretouch-contacts"),
                    publicDomain = json.optString("publicDomain")
                )
            } else {
                R2Config()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading R2 config", e)
            R2Config()
        }
    }

    suspend fun saveConfig(newConfig: R2Config) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("accountId", newConfig.accountId.trim())
                put("accessKeyId", newConfig.accessKeyId.trim())
                put("secretAccessKey", newConfig.secretAccessKey.trim())
                put("bucketName", newConfig.bucketName.trim().ifBlank { "caretouch-contacts" })
                put("publicDomain", newConfig.publicDomain.trim().removeSuffix("/"))
            }
            configFile.writeText(json.toString(2))
            _config.value = newConfig
        } catch (e: Exception) {
            Log.e(TAG, "Error saving R2 config", e)
        }
    }

    /**
     * Compresses and uploads a local contact photo to Cloudflare R2.
     * Returns the public CDN URL or null if upload failed or R2 not configured.
     */
    suspend fun uploadContactPhoto(
        localPhotoUriString: String,
        contactId: String,
        userId: String
    ): String? = withContext(Dispatchers.IO) {
        val r2 = _config.value
        if (!r2.isConfigured) {
            Log.d(TAG, "Cloudflare R2 is not configured, skipping cloud photo upload")
            return@withContext null
        }

        try {
            val uri = Uri.parse(localPhotoUriString)
            val inputStream = appContext.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            inputStream.close()

            // Scale to max 512x512
            val maxDim = 512
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDim || height > maxDim) {
                maxDim.toFloat() / maxOf(width, height)
            } else 1.0f

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else originalBitmap

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val bytes = baos.toByteArray()
            if (scaledBitmap != originalBitmap) originalBitmap.recycle()

            val objectKey = "users/$userId/contacts/contact_${contactId}.jpg"
            val uploaded = putObject(
                config = r2,
                key = objectKey,
                data = bytes,
                contentType = "image/jpeg"
            )

            if (uploaded) {
                val publicUrl = if (r2.publicDomain.isNotBlank()) {
                    "${r2.publicDomain.removeSuffix("/")}/$objectKey"
                } else {
                    "${r2.endpoint}/${r2.bucketName}/$objectKey"
                }
                Log.i(TAG, "Successfully uploaded contact photo to R2: $publicUrl")
                publicUrl
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception uploading contact photo to R2", e)
            null
        }
    }

    /**
     * Executes an AWS SigV4 PutObject request against Cloudflare R2
     */
    private fun putObject(
        config: R2Config,
        key: String,
        data: ByteArray,
        contentType: String
    ): Boolean {
        val region = "auto"
        val service = "s3"
        val host = "${config.accountId}.r2.cloudflarestorage.com"
        val url = "https://$host/${config.bucketName}/$key"

        val now = Date()
        val isoDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val dateOnlyFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

        val amzDate = isoDateFormat.format(now)
        val dateStamp = dateOnlyFormat.format(now)

        val payloadHash = sha256Hex(data)

        val canonicalHeaders = "content-type:$contentType\nhost:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$amzDate\n"
        val signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date"
        val canonicalUri = "/${config.bucketName}/$key"

        val canonicalRequest = "PUT\n$canonicalUri\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"

        val credentialScope = "$dateStamp/$region/$service/aws4_request"
        val stringToSign = "AWS4-HMAC-SHA256\n$amzDate\n$credentialScope\n${sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))}"

        val signingKey = getSignatureKey(config.secretAccessKey, dateStamp, region, service)
        val signature = hmacSha256Hex(signingKey, stringToSign)

        val authorizationHeader = "AWS4-HMAC-SHA256 Credential=${config.accessKeyId}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

        val request = Request.Builder()
            .url(url)
            .put(data.toRequestBody(contentType.toMediaType()))
            .header("Host", host)
            .header("x-amz-date", amzDate)
            .header("x-amz-content-sha256", payloadHash)
            .header("Content-Type", contentType)
            .header("Authorization", authorizationHeader)
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val isSuccess = response.isSuccessful
            if (!isSuccess) {
                Log.e(TAG, "R2 PutObject failed with HTTP ${response.code}: ${response.body?.string()}")
            }
            response.close()
            isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "R2 PutObject network error", e)
            false
        }
    }

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).joinToString("") { "%02x".format(it) }
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kSecret = ("AWS4$key").toByteArray(Charsets.UTF_8)
        val kDate = hmacSha256(kSecret, dateStamp)
        val kRegion = hmacSha256(kDate, regionName)
        val kService = hmacSha256(kRegion, serviceName)
        return hmacSha256(kService, "aws4_request")
    }

    companion object {
        private const val TAG = "R2StorageManager"
    }
}
