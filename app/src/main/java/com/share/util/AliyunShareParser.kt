package com.share.util

import com.share.data.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class AliyunShareParser {

    private val sampleBooks = listOf(
        Book(
            title = "人类简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5086-4735-7",
            source = "阿里云分享",
            shareUrl = "https://www.alipan.com/s/2bACfCjLCkH"
        ),
        Book(
            title = "未来简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5086-4896-5",
            source = "阿里云分享",
            shareUrl = "https://www.alipan.com/s/2bACfCjLCkH"
        ),
        Book(
            title = "今日简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5217-1000-6",
            source = "阿里云分享",
            shareUrl = "https://www.alipan.com/s/2bACfCjLCkH"
        )
    )

    suspend fun parseShareUrl(shareUrl: String): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val url = URL(shareUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val html = connection.inputStream.bufferedReader().readText()

                val title = extractPattern(html, "<title>(.*?)</title>")
                    ?: extractPattern(html, "\"fileName\"\\s*:\\s*\"([^\"]+)\"")
                    ?: "未知书名"

                val author = extractPattern(html, "\"author\"\\s*:\\s*\"([^\"]+)\"")
                    ?: "未知作者"

                val isbn = extractPattern(html, "\"isbn\"\\s*:\\s*\"([^\"]+)\"")
                    ?: extractPattern(html, "ISBN[:\\s]*([\\d-]+)")
                    ?: ""

                val book = Book(
                    title = cleanHtml(title),
                    author = cleanHtml(author),
                    isbn = isbn,
                    source = "阿里云分享",
                    shareUrl = shareUrl
                )

                Result.success(book)
            } else {
                Result.failure(Exception("HTTP error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSampleBooks(): List<Book> = sampleBooks

    fun searchBooksByKeyword(keyword: String): List<Book> {
        val lowerKeyword = keyword.lowercase()
        return sampleBooks.filter { book ->
            book.title.lowercase().contains(lowerKeyword) ||
            book.author.lowercase().contains(lowerKeyword) ||
            book.isbn.contains(keyword)
        }
    }

    private fun extractPattern(html: String, pattern: String): String? {
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        return match?.groupValues?.getOrNull(1)
    }

    private fun cleanHtml(text: String): String {
        return text
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("&[a-zA-Z]+;"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
