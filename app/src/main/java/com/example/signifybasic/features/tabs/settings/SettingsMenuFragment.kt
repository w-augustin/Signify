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

        view.findViewById<MaterialCardView>(R.id.btn_account).setOnClickListener {
            openFragment(SettingsAccountFragment())
        }

        view.findViewById<MaterialCardView>(R.id.btn_preferences).setOnClickListener {
            openFragment(SettingsPreferencesFragment())
        }

        view.findViewById<MaterialCardView>(R.id.btn_notifications).setOnClickListener {
            openFragment(SettingsNotificationsFragment())
        }

        view.findViewById<MaterialCardView>(R.id.btn_accessibility).setOnClickListener {
            openFragment(SettingsAccessibilityFragment())
        }

        view.findViewById<MaterialCardView>(R.id.btn_help).setOnClickListener {
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
