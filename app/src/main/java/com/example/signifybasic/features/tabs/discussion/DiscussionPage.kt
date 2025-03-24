package com.example.signifybasic.features.tabs.discussion

import android.os.Bundle
import com.example.signifybasic.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signifybasic.databinding.DiscussionActivityBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog

class DiscussionPage : AppCompatActivity() {

    private lateinit var binding: DiscussionActivityBinding

    private fun showNewPostDialog(onSubmit: (String) -> Unit) {
        val view = layoutInflater.inflate(R.layout.discussion_new_post, null)
        val editText = view.findViewById<TextInputEditText>(R.id.editPostTitle)
        val submitButton = view.findViewById<Button>(R.id.submitPostButton)

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        submitButton.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isNotEmpty()) {
                onSubmit(text)
                dialog.dismiss()
            } else {
                editText.error = "Please enter something!"
            }
        }

        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DiscussionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val jsonString = assets.open("discussions.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<DiscussionPost>>() {}.type
        val posts = Gson().fromJson<List<DiscussionPost>>(jsonString, listType).toMutableList()

        binding.discussionRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DiscussionAdapter(posts)
        binding.discussionRecyclerView.adapter = adapter

        binding.fabNewPost.setOnClickListener {
            showNewPostDialog { newPostTitle ->
                val newPost = DiscussionPost(
                    title = newPostTitle,
                    meta = "Just now | 0 replies"
                )
                posts.add(0, newPost)
                adapter.notifyItemInserted(0)
                binding.discussionRecyclerView.scrollToPosition(0)
            }
        }
    }
}
