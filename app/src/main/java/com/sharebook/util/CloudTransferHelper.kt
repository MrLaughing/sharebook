package com.sharebook.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudTransferHelper(private val context: Context) {

    suspend fun transferToAliyun(shareUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Result.success(shareUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transferTo115(shareUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val transferUrl = "https://115.com"
            Result.success(transferUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
