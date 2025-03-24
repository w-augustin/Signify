package com.example.signifybasic.features.tabs.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R

class SettingsNotificationsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notifications_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.setPadding(32, 32, 32, 32)
        listView.clipToPadding = false
        listView.setBackgroundColor(resources.getColor(android.R.color.white, null))
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null || key == null) return

        when (key) {
            "push_notifications" -> {
                val enabled = sharedPreferences.getBoolean(key, true)
                Log.d("Settings", "Push notifications enabled: $enabled")
            }

            "email_updates" -> {
                val enabled = sharedPreferences.getBoolean(key, false)
                Log.d("Settings", "Email updates enabled: $enabled")
            }

            "reminder_time" -> {
                val time = sharedPreferences.getString(key, "08:00")
                Log.d("Settings", "Reminder time changed to: $time")
            }
        }
    }

}
