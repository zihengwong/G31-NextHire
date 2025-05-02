package com.example.nexthire.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

object ImageUtils {

    fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteOldProfileImages(context: Context) {
        val files = context.filesDir.listFiles() ?: return
        for (file in files) {
            if (file.name.startsWith("profile_") && file.name.endsWith(".jpg")) {
                file.delete()
            }
        }
    }
}
