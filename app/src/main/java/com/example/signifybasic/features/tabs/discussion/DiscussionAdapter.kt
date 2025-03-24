package com.example.signifybasic.features.tabs.discussion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.signifybasic.databinding.DiscussionItemPostBinding

class DiscussionAdapter(
    private val posts: List<DiscussionPost>
) : RecyclerView.Adapter<DiscussionAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: DiscussionItemPostBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: DiscussionPost) {
            binding.postTitle.text = post.title
            binding.postMeta.text = post.meta
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = DiscussionItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}
