package com.sharebook.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudTransferHelper(private val context: Context) {

    fun openAliyunPan(shareUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shareUrl))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openWebPage(shareUrl)
        }
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSaveOptions(shareUrl: String) {
        openAliyunPan(shareUrl)
    }
}
