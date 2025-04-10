package com.example.signifybasic.features.tabs.resources

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signifybasic.databinding.ResourcesItemVideoBinding

class ResourcesAdapter(
    private val items: MutableList<ResourceItem>
) : RecyclerView.Adapter<ResourcesAdapter.ResourceViewHolder>() {

    inner class ResourceViewHolder(private val binding: ResourcesItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResourceItem) {
            binding.resourceTitle.text = item.title
            Glide.with(binding.resourceThumbnail.context)
                .load(item.thumbnail)
                .into(binding.resourceThumbnail)

            binding.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ResourcesItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<ResourceItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
