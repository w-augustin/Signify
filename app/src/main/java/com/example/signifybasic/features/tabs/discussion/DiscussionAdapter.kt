package com.example.signifybasic.features.tabs.discussion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.signifybasic.databinding.DiscussionItemPostBinding
import com.example.signifybasic.features.tabs.discussion.DiscussionPost

class DiscussionAdapter(
    private val posts: MutableList<DiscussionPost>
) : RecyclerView.Adapter<DiscussionAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: DiscussionItemPostBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: DiscussionPost) {
            binding.postTitle.text = post.content
            binding.postMeta.text = "User #${post.userId} • ${post.timestamp}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = DiscussionItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    fun updateData(newPosts: List<DiscussionPost>) {
        (posts as? MutableList<DiscussionPost>)?.apply {
            clear()
            addAll(newPosts)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}
