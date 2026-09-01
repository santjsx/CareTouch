package com.example.amma.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object PhotoStorageHelper {
    fun savePhotoLocally(context: Context, sourceUri: Uri, contactId: String): String? {
        return try {
            val dir = File(context.filesDir, "contact_photos").apply {
                if (!exists()) mkdirs()
            }
            val destFile = File(dir, "contact_${contactId}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input) ?: return null
                val maxDim = maxOf(bitmap.width, bitmap.height)
                val scaled = if (maxDim > 1024) {
                    val scale = 1024f / maxDim
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    )
                } else {
                    bitmap
                }

                FileOutputStream(destFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmap(photoPath: String?): Bitmap? {
        if (photoPath.isNullOrBlank()) return null
        return try {
            val file = File(photoPath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
