package com.share

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.share.data.model.Book
import com.share.data.model.SearchType
import com.share.databinding.ActivityMainBinding
import com.share.ui.adapter.BookAdapter
import com.share.util.AliyunShareParser
import com.share.util.CloudTransferHelper
import com.share.util.FileDownloader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var aliyunShareParser: AliyunShareParser
    private lateinit var fileDownloader: FileDownloader
    private lateinit var cloudTransferHelper: CloudTransferHelper

    private val bookList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupRecyclerView()
        setupSearchInput()
        setupSearchTypeChips()
        
        // 初始显示示例数据
        showSampleBooks()
    }

    private fun initializeComponents() {
        aliyunShareParser = AliyunShareParser()
        fileDownloader = FileDownloader(this)
        cloudTransferHelper = CloudTransferHelper(this)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onDownloadClick = { book -> downloadBook(book) },
            onShareClick = { book -> showTransferOptions(book) }
        )

        binding.resultsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bookAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchInput() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    performSearch(s.toString())
                } else {
                    showSampleBooks()
                }
            }
        })
    }

    private fun setupSearchTypeChips() {
        binding.searchTypeGroup.setOnCheckedStateChangeListener { _, _ ->
            val currentText = binding.searchInput.text.toString()
            if (!currentText.isEmpty()) {
                performSearch(currentText)
            } else {
                showSampleBooks()
            }
        }
    }

    private fun getCurrentSearchType(): SearchType {
        return when (binding.searchTypeGroup.checkedChipId) {
            R.id.chipAuthor -> SearchType.AUTHOR
            R.id.chipIsbn -> SearchType.ISBN
            else -> SearchType.BOOK_NAME
        }
    }

    private fun showSampleBooks() {
        val books = aliyunShareParser.getSampleBooks()
        updateBookList(books)
    }

    private fun performSearch(keyword: String) {
        showLoading(true)

        lifecycleScope.launch {
            val searchType = getCurrentSearchType()
            val filteredBooks = filterBooksByType(aliyunShareParser.searchBooksByKeyword(keyword), searchType, keyword)
            updateBookList(filteredBooks)
            showLoading(false)
        }
    }

    private fun filterBooksByType(books: List<Book>, type: SearchType, keyword: String): List<Book> {
        val lowerKeyword = keyword.lowercase()
        return books.filter { book ->
            when (type) {
                SearchType.BOOK_NAME -> book.title.lowercase().contains(lowerKeyword)
                SearchType.AUTHOR -> book.author.lowercase().contains(lowerKeyword)
                SearchType.ISBN -> book.isbn.contains(keyword)
            }
        }
    }

    private fun updateBookList(books: List<Book>) {
        bookList.clear()
        bookList.addAll(books)
        bookAdapter.submitList(bookList.toList())

        if (books.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.emptyState.text = "未找到相关图书"
        } else {
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun downloadBook(book: Book) {
        lifecycleScope.launch {
            val result = fileDownloader.downloadFile(book.shareUrl, "${book.title}.pdf")

            result.onSuccess { file ->
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "下载完成: ${file.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                    fileDownloader.openFile(file)
                }
            }

            result.onFailure { error ->
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "下载失败: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showTransferOptions(book: Book) {
        val options = arrayOf("阿里云盘", "115网盘", "百度网盘")

        AlertDialog.Builder(this)
            .setTitle("选择网盘")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> transferToAliyun(book)
                    1 -> transferTo115(book)
                    2 -> transferToBaidu(book)
                }
            }
            .show()
    }

    private fun transferToAliyun(book: Book) {
        lifecycleScope.launch {
            val result = cloudTransferHelper.transferToAliyun(book.shareUrl)
            result.onSuccess { url ->
                runOnUiThread {
                    cloudTransferHelper.openUrl(url)
                }
            }
        }
    }

    private fun transferTo115(book: Book) {
        lifecycleScope.launch {
            val result = cloudTransferHelper.transferTo115(book.shareUrl)
            result.onSuccess { url ->
                runOnUiThread {
                    cloudTransferHelper.openUrl(url)
                }
            }
        }
    }

    private fun transferToBaidu(book: Book) {
        val baiduUrl = "https://pan.baidu.com"
        cloudTransferHelper.openUrl(baiduUrl)
    }
}
