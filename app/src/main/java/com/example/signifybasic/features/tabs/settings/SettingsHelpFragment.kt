package com.example.signifybasic.features.tabs.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.signifybasic.R

class SettingsHelpFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.help_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.setPadding(32, 32, 32, 32)
        listView.clipToPadding = false
        listView.setBackgroundColor(resources.getColor(android.R.color.white, null))

        findPreference<Preference>("faq")?.setOnPreferenceClickListener {
            Log.d("SettingsHelp", "FAQ clicked")
            true
        }

        findPreference<Preference>("contact_support")?.setOnPreferenceClickListener {
            Log.d("SettingsHelp", "Contact Support clicked")
            true
        }

        findPreference<Preference>("terms")?.setOnPreferenceClickListener {
            Log.d("SettingsHelp", "Terms & Conditions clicked")
            true
        }

        findPreference<Preference>("privacy")?.setOnPreferenceClickListener {
            Log.d("SettingsHelp", "Privacy Policy clicked")
            true
        }
    }
}
