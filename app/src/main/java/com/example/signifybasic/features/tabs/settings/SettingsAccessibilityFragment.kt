package com.example.signifybasic.features.tabs.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsAccessibilityFragment : Fragment(R.layout.accessibility_preferences) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        // Make content draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        // Apply padding to avoid overlap with status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val contrastSwitch = view.findViewById<MaterialSwitch>(R.id.switch_high_contrast)

        contrastSwitch.isChecked = prefs.getBoolean("high_contrast_enabled", false)

        contrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("high_contrast_enabled", isChecked).apply()

            if (isChecked) {
                applyHighContrastToAllViews(view, requireContext())
            } else {
                requireActivity().recreate()
//                removeHighContrastFromAllViews(view, requireContext())
            }
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_text_size)

        when (prefs.getString("text_size", "medium")) {
            "small" -> radioGroup.check(R.id.radio_small)
            "medium" -> radioGroup.check(R.id.radio_medium)
            "large" -> radioGroup.check(R.id.radio_large)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val sizePref = when (checkedId) {
                R.id.radio_small -> "small"
                R.id.radio_medium -> "medium"
                R.id.radio_large -> "large"
                else -> "medium"
            }
            prefs.edit().putString("text_size", sizePref).apply()
            Toast.makeText(requireContext(), "Text size set to $sizePref", Toast.LENGTH_SHORT).show()
        }

//
//        view.findViewById<MaterialButton>(R.id.btn_delete_account).setOnClickListener {
//            // TODO: Delete account logic
//        }
    }

}