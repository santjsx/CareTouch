package com.example.amma.ota

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.amma.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSizeBytes: Long
) {
    val apkSizeMb: Double get() = apkSizeBytes / (1024.0 * 1024.0)
}

sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    data class UpdateAvailable(val info: UpdateInfo) : UpdateStatus
    data class UpToDate(val version: String) : UpdateStatus
    data class Downloading(val progressPercent: Int, val downloadedMb: Double, val totalMb: Double) : UpdateStatus
    data class ReadyToInstall(val apkFile: File) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

class OtaUpdateManager(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    suspend fun checkForUpdates(owner: String = "santjsx", repo: String = "CareTouch"): UpdateStatus {
        return withContext(Dispatchers.IO) {
            _updateStatus.value = UpdateStatus.Checking
            try {
                val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "CareTouch-OTA-Updater")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    val status = if (response.code == 404) {
                        UpdateStatus.UpToDate(BuildConfig.VERSION_NAME)
                    } else {
                        UpdateStatus.Error("GitHub API response code: ${response.code}")
                    }
                    _updateStatus.value = status
                    return@withContext status
                }

                val bodyString = response.body?.string() ?: run {
                    val status = UpdateStatus.Error("Empty response from GitHub")
                    _updateStatus.value = status
                    return@withContext status
                }

                val json = JSONObject(bodyString)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val releaseName = json.optString("name", "Version $tagName")
                val releaseNotes = json.optString("body", "No release notes provided.")
                val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()

                // Find APK in release assets
                val assets = json.optJSONArray("assets")
                var apkDownloadUrl = ""
                var apkSize = 0L

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                val isNewer = isVersionNewer(tagName, currentVersion)
                val status = if (isNewer && apkDownloadUrl.isNotBlank()) {
                    UpdateStatus.UpdateAvailable(
                        UpdateInfo(
                            hasUpdate = true,
                            currentVersion = BuildConfig.VERSION_NAME,
                            latestVersion = tagName,
                            releaseTitle = releaseName,
                            releaseNotes = releaseNotes,
                            downloadUrl = apkDownloadUrl,
                            apkSizeBytes = apkSize
                        )
                    )
                } else {
                    UpdateStatus.UpToDate(BuildConfig.VERSION_NAME)
                }

                _updateStatus.value = status
                status
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for OTA update", e)
                val status = UpdateStatus.Error("Update check failed: ${e.localizedMessage ?: "Network error"}")
                _updateStatus.value = status
                status
            }
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo) {
        withContext(Dispatchers.IO) {
            try {
                val otaDir = File(context.cacheDir, "ota").apply { mkdirs() }
                val targetFile = File(otaDir, "CareTouch-v${info.latestVersion}.apk")

                val request = Request.Builder()
                    .url(info.downloadUrl)
                    .header("User-Agent", "CareTouch-OTA-Updater")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    _updateStatus.value = UpdateStatus.Error("Download failed with HTTP ${response.code}")
                    return@withContext
                }

                val body = response.body ?: run {
                    _updateStatus.value = UpdateStatus.Error("Empty download body")
                    return@withContext
                }

                val contentLength = body.contentLength().takeIf { it > 0 } ?: info.apkSizeBytes
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val percent = if (contentLength > 0) ((totalBytesRead * 100) / contentLength).toInt() else 0
                    val downloadedMb = totalBytesRead / (1024.0 * 1024.0)
                    val totalMb = contentLength / (1024.0 * 1024.0)

                    _updateStatus.value = UpdateStatus.Downloading(percent, downloadedMb, totalMb)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _updateStatus.value = UpdateStatus.ReadyToInstall(targetFile)

                withContext(Dispatchers.Main) {
                    installApk(targetFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading OTA update", e)
                _updateStatus.value = UpdateStatus.Error("Download failed: ${e.localizedMessage}")
            }
        }
    }

    fun installApk(apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering APK installer", e)
            _updateStatus.value = UpdateStatus.Error("Installation trigger failed: ${e.localizedMessage}")
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    companion object {
        private const val TAG = "OtaUpdateManager"
    }
}
