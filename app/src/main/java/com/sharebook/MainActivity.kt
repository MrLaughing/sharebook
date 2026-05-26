package com.sharebook

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharebook.data.model.Book
import com.sharebook.data.model.SearchType
import com.sharebook.databinding.ActivityMainBinding
import com.sharebook.ui.adapter.BookAdapter
import com.sharebook.util.AliyunShareParser
import com.sharebook.util.CloudTransferHelper
import com.sharebook.util.FileDownloader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var aliyunShareParser: AliyunShareParser
    private lateinit var fileDownloader: FileDownloader
    private lateinit var cloudTransferHelper: CloudTransferHelper

    private val STORAGE_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupRecyclerView()
        setupSearchButton()
        setupSearchTypeChips()

        checkStoragePermission()
        showSampleBooks()
    }

    private fun initializeComponents() {
        aliyunShareParser = AliyunShareParser()
        fileDownloader = FileDownloader(this)
        cloudTransferHelper = CloudTransferHelper(this)

        val downloadDir = fileDownloader.getDownloadDir()
        binding.downloadPathHint.text = "下载: ${downloadDir.absolutePath}"
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onDownloadClick = { book -> downloadBook(book) },
            onSaveClick = { book -> saveToCloud(book) }
        )

        binding.resultsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bookAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString().trim()
            if (query.isEmpty()) {
                showSampleBooks()
                return@setOnClickListener
            }

            performSearch(query)
        }
    }

    private fun setupSearchTypeChips() {
        binding.searchTypeGroup.setOnCheckedStateChangeListener { _, _ ->
            val query = binding.searchInput.text.toString().trim()
            if (query.isNotEmpty() && !aliyunShareParser.isShareUrl(query)) {
                performSearch(query)
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
        binding.resultCount.text = "共找到 ${books.size} 本图书"
    }

    private fun performSearch(query: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                if (aliyunShareParser.isShareUrl(query)) {
                    val books = aliyunShareParser.parseShareUrlToBooks(query)
                    updateBookList(books)
                    binding.resultCount.text = "从分享链接获取 ${books.size} 本图书"
                } else {
                    val searchType = getCurrentSearchType()
                    val books = aliyunShareParser.searchBooksByKeyword(query, searchType)
                    updateBookList(books)
                    binding.resultCount.text = "共找到 ${books.size} 本图书"
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "搜索出错: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateBookList(books: List<Book>) {
        bookAdapter.submitList(books)

        if (books.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.emptyText.text = "未找到相关图书"
            binding.resultCount.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.resultCount.visibility = View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingView.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            binding.resultsList.visibility = View.GONE
        } else {
            binding.loadingView.visibility = View.GONE
            binding.resultsList.visibility = View.VISIBLE
        }
    }

    private fun downloadBook(book: Book) {
        lifecycleScope.launch {
            if (!checkStoragePermission()) {
                requestStoragePermission()
                return@launch
            }

            val result = fileDownloader.downloadBook(book)
            
            result.onSuccess { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@MainActivity,
                    "下载失败: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                fileDownloader.openShareUrl(book.shareUrl)
            }
        }
    }

    private fun saveToCloud(book: Book) {
        cloudTransferHelper.openAliyunPan(book.shareUrl)
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要存储权限才能下载", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
