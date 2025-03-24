package com.example.signifybasic.features.tabs.resources

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.signifybasic.databinding.ResourcesItemLinkBinding

class LinksAdapter(private val items: List<LinkItem>) :
    RecyclerView.Adapter<LinksAdapter.LinkViewHolder>() {

    inner class LinkViewHolder(private val binding: ResourcesItemLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LinkItem) {
            binding.linkButton.text = item.title
            binding.linkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val binding = ResourcesItemLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LinkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
