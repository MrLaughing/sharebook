package com.sharebook.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileDownloader(private val context: Context) {

    fun getDownloadDir(): File {
        val downloadDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ShareBook"
        )
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        return downloadDir
    }

    suspend fun downloadBook(book: com.sharebook.data.model.Book): Result<String> = withContext(Dispatchers.IO) {
        try {
            val downloadDir = getDownloadDir()
            val fileName = "${book.title}.${book.format.lowercase()}"
            val targetFile = File(downloadDir, fileName)

            val downloadManager = context.getSystemService<DownloadManager>()
            val uri = Uri.parse(book.shareUrl)

            val request = DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("正在下载 ${book.title}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(targetFile))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            withContext(Dispatchers.Main) {
                downloadManager?.enqueue(request)
                Toast.makeText(
                    context,
                    "开始下载到: ${downloadDir.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }

            Result.success("下载已开始，文件保存至: ${downloadDir.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openShareUrl(shareUrl: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(shareUrl))
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "请安装阿里云盘APP", Toast.LENGTH_SHORT).show()
        }
    }
}
