package com.example.signifybasic.features.tabs.discussion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.databinding.DiscussionActivityBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DiscussionFragment : Fragment() {

    private var _binding: DiscussionActivityBinding? = null
    private val binding get() = _binding!!

    private fun showNewPostDialog(onSubmit: (String) -> Unit) {
        val view = layoutInflater.inflate(R.layout.discussion_new_post, null)
        val editText = view.findViewById<TextInputEditText>(R.id.editPostTitle)
        val submitButton = view.findViewById<Button>(R.id.submitPostButton)

        val dialog = BottomSheetDialog(requireContext())
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiscussionActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dbHelper = DBHelper(requireContext())

        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        val dbPosts = dbHelper.getAllDiscussionPosts()
        val adapter = DiscussionAdapter(dbPosts.toMutableList())

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.discussionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.discussionRecyclerView.adapter = adapter

        binding.fabNewPost.setOnClickListener {
            showNewPostDialog { newPostContent ->
                val success = dbHelper.addDiscussionPost(
                    userID = 1, // Replace with real user ID
                    content = newPostContent
                )

                if (success) {
                    val updatedPosts = dbHelper.getAllDiscussionPosts()
                    adapter.updateData(updatedPosts.toMutableList())
                    binding.discussionRecyclerView.scrollToPosition(0)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
