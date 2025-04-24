package com.example.signifybasic.features.tabs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.signifybasic.R
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView

class SettingsMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.settings_fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // basic xml setup
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // navigate to account settings
        view.findViewById<MaterialCardView>(R.id.btn_account).setOnClickListener {
            openFragment(SettingsAccountFragment())
        }

        // navigate to prefernce settings
        view.findViewById<MaterialCardView>(R.id.btn_preferences).setOnClickListener {
            openFragment(SettingsPreferencesFragment())
        }

        // navigate to notification settings
        view.findViewById<MaterialCardView>(R.id.btn_notifications).setOnClickListener {
            openFragment(SettingsNotificationsFragment())
        }

        // navigate to accessibility settings
        view.findViewById<MaterialCardView>(R.id.btn_accessibility).setOnClickListener {
            openFragment(SettingsAccessibilityFragment())
        }

        // navigate to help settings
        view.findViewById<MaterialCardView>(R.id.btn_help).setOnClickListener {
            openFragment(SettingsHelpFragment())
        }
    }

    // helper function to facilitate navigation
    private fun openFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
