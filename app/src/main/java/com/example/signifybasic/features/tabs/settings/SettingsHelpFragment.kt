package com.example.signifybasic.features.tabs.settings

import android.os.Bundle
import android.view.View
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
    }
}
