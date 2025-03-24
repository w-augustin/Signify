package com.example.signifybasic.features.tabs.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.example.signifybasic.R

class SettingsAccessibilityFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.accessibility_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.setPadding(32, 32, 32, 32)
        listView.clipToPadding = false
        listView.setBackgroundColor(resources.getColor(android.R.color.white, null))
    }
}
