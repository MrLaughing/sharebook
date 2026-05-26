package com.sharebook.data.model

data class Book(
    val title: String,
    val author: String,
    val isbn: String,
    val format: String = "PDF",
    val source: String = "阿里云盘",
    val shareUrl: String,
    val fileId: String? = null,
    val size: Long = 0,
    val downloadUrl: String? = null
)
