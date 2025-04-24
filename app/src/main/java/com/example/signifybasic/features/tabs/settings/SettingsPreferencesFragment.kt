package com.example.signifybasic.features.tabs.settings

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsPreferencesFragment : Fragment(R.layout.app_preferences) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // basic xml
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        // make content draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // enable/disable sound effects
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchSound = view.findViewById<MaterialSwitch>(R.id.switch_sound_effects)

        switchSound.isChecked = prefs.getBoolean("sound_effects_enabled", true)

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_effects_enabled", isChecked).apply()
        }

        // enable/disable vibration
        val switchVibration = view.findViewById<MaterialSwitch>(R.id.switch_vibration)
        switchVibration.isChecked = prefs.getBoolean("vibration_enabled", true)

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        // reset account progress
        val btnClearProgress = view.findViewById<MaterialButton>(R.id.clear_progress)

        btnClearProgress.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Reset")
                .setMessage("Are you sure you want to delete all your progress?")
                .setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(requireContext(), "Progress reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


//
//        view.findViewById<MaterialButton>(R.id.btn_delete_account).setOnClickListener {
//            // TODO: Delete account logic
//        }
    }

}