package com.example.signifybasic.features.tabs.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.signifybasic.R
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.tabs.discussion.DiscussionPage
import com.example.signifybasic.features.tabs.settings.SettingsPage

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "onCreateView: HomeFragment loaded")

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val cardSettings = view.findViewById<CardView>(R.id.card_settings)
        val cardDiscussion = view.findViewById<CardView>(R.id.card_discussion)
        val cardActivityCenter = view.findViewById<CardView>(R.id.card_activity_center)
        val cardPlayground = view.findViewById<CardView>(R.id.card_playground)

        cardSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsPage::class.java))
        }

        cardDiscussion.setOnClickListener {
            startActivity(Intent(requireContext(), DiscussionPage::class.java))
        }

        cardActivityCenter.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityCenter::class.java))
        }

        cardPlayground.setOnClickListener {
            // Your playground action
        }

        return view
    }
}
