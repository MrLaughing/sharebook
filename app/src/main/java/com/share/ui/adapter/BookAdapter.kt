package com.share.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.share.R
import com.share.data.model.Book
import com.share.databinding.ItemBookBinding

class BookAdapter(
    private val onDownloadClick: (Book) -> Unit,
    private val onShareClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = "作者: ${book.author}"
            binding.bookIsbn.text = "ISBN: ${book.isbn}"
            binding.bookSource.text = "来源: ${book.source}"

            binding.downloadBtn.setOnClickListener {
                onDownloadClick(book)
            }

            binding.shareBtn.setOnClickListener {
                onShareClick(book)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.shareUrl == newItem.shareUrl
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}
