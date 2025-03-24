package com.example.signifybasic.features.tabs.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R

class SettingsPreferencesFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("SettingsPrefs", "Changed preference: $key")

        when (key) {
            "theme" -> {
                val theme = sharedPreferences?.getString(key, "system")
                Log.d("SettingsPrefs", "Theme updated to: $theme")
            }
            "auto_play_videos" -> {
                val autoplay = sharedPreferences?.getBoolean(key, true)
                Log.d("SettingsPrefs", "Autoplay toggled: $autoplay")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setPadding(32, 32, 32, 32)
        listView.clipToPadding = false
        listView.setBackgroundColor(resources.getColor(android.R.color.white, null))
    }
}
