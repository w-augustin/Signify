package com.example.signifybasic.features.tabs.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.signifybasic.R
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.tabs.playground.PlaygroundActivity
import com.example.signifybasic.features.tabs.dictionary.DictionaryFragment
import com.example.signifybasic.features.tabs.resources.ResourcesFragment
import com.example.signifybasic.features.tabs.settings.SettingsFragment
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        //Retrieve username to assign text
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null)

        val textView = view.findViewById<TextView>(R.id.textView)
        val originalText = textView.text.toString() // "Welcome {User}"
        val personalizedText = originalText.replace("{User}", username ?: "Guest")

        textView.text = personalizedText

        // set up basic xml
        val cardSettings = view.findViewById<CardView>(R.id.card_settings)
        val cardDictionary = view.findViewById<CardView>(R.id.card_dictionary)
        val cardActivityCenter = view.findViewById<CardView>(R.id.card_activity_center)
        val cardPlayground = view.findViewById<CardView>(R.id.card_playground)
        val cardResources = view.findViewById<CardView>(R.id.card_resources)
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        // allow user navigation among cards
        // navigate to settings
        cardSettings.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
        // navigate to dictionary
        cardDictionary.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DictionaryFragment())
                .addToBackStack(null)
                .commit()
        }
        // navigate to resources
        cardResources.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ResourcesFragment())
                .addToBackStack(null)
                .commit()
        }
        // navigate to activity center
        cardActivityCenter.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityCenter::class.java))
        }
        // navigate to playground
        cardPlayground.setOnClickListener {
            startActivity(Intent(requireContext(), PlaygroundActivity::class.java))
        }

        return view
    }
}
