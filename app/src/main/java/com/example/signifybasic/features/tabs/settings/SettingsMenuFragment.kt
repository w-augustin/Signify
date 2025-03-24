package com.example.signifybasic.features.tabs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.signifybasic.R

class SettingsMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.settings_fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_account).setOnClickListener {
            openFragment(SettingsAccountFragment())
        }

        view.findViewById<Button>(R.id.btn_preferences).setOnClickListener {
            openFragment(SettingsPreferencesFragment())
        }

        view.findViewById<Button>(R.id.btn_notifications).setOnClickListener {
            openFragment(SettingsNotificationsFragment())
        }

        view.findViewById<Button>(R.id.btn_accessibility).setOnClickListener {
            openFragment(SettingsAccessibilityFragment())
        }

        view.findViewById<Button>(R.id.btn_help).setOnClickListener {
            openFragment(SettingsHelpFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
