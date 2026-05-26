package com.share.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FileDownloader(private val context: Context) {

    suspend fun downloadFile(url: String, fileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir

            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val file = File(downloadDir, fileName)

            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openFile(file: File) {
        val mimeType = when {
            file.name.endsWith(".pdf") -> "application/pdf"
            file.name.endsWith(".epub") -> "application/epub+zip"
            file.name.endsWith(".txt") -> "text/plain"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(android.net.Uri.fromFile(file), mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
