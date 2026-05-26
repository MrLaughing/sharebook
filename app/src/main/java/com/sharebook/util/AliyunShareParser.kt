package com.sharebook.util

import com.sharebook.data.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class AliyunShareParser {

    private val sampleShareUrl = "https://www.alipan.com/s/2bACfCjLCkH"

    private val sampleBooks = listOf(
        Book(
            title = "人类简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5086-4735-7",
            format = "PDF",
            source = "阿里云盘",
            shareUrl = sampleShareUrl,
            fileId = "file1",
            size = 25 * 1024 * 1024
        ),
        Book(
            title = "未来简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5086-4896-5",
            format = "PDF",
            source = "阿里云盘",
            shareUrl = sampleShareUrl,
            fileId = "file2",
            size = 22 * 1024 * 1024
        ),
        Book(
            title = "今日简史",
            author = "尤瓦尔·赫拉利",
            isbn = "978-7-5217-1000-6",
            format = "PDF",
            source = "阿里云盘",
            shareUrl = sampleShareUrl,
            fileId = "file3",
            size = 28 * 1024 * 1024
        ),
        Book(
            title = "百年孤独",
            author = "加西亚·马尔克斯",
            isbn = "978-7-5442-5399-8",
            format = "EPUB",
            source = "阿里云盘",
            shareUrl = sampleShareUrl,
            fileId = "file4",
            size = 3 * 1024 * 1024
        ),
        Book(
            title = "活着",
            author = "余华",
            isbn = "978-7-5063-3021-6",
            format = "PDF",
            source = "阿里云盘",
            shareUrl = sampleShareUrl,
            fileId = "file5",
            size = 8 * 1024 * 1024
        )
    )

    fun getSampleBooks(): List<Book> = sampleBooks

    fun searchBooksByKeyword(keyword: String, searchType: com.sharebook.data.model.SearchType): List<Book> {
        val lowerKeyword = keyword.lowercase()
        return sampleBooks.filter { book ->
            when (searchType) {
                com.sharebook.data.model.SearchType.BOOK_NAME -> 
                    book.title.lowercase().contains(lowerKeyword)
                com.sharebook.data.model.SearchType.AUTHOR -> 
                    book.author.lowercase().contains(lowerKeyword)
                com.sharebook.data.model.SearchType.ISBN -> 
                    book.isbn.contains(keyword)
            }
        }
    }

    fun isShareUrl(input: String): Boolean {
        return input.contains("alipan.com") || 
               input.contains("aliyundrive.com") ||
               input.contains("www.alipan.com")
    }

    fun parseShareUrlToBooks(shareUrl: String): List<Book> {
        return sampleBooks.map { book ->
            book.copy(shareUrl = shareUrl)
        }
    }

    fun getBookDownloadUrl(book: Book): String {
        return book.shareUrl
    }
}
