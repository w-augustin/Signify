package com.example.signifybasic.features.tabs.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SettingsHelpFragment : Fragment(R.layout.help_preferences) {

    // display / hide an faq section
    fun toggleFaq(answerView: TextView, arrowIcon: ImageView) {
        val isVisible = answerView.visibility == View.VISIBLE
        answerView.visibility = if (isVisible) View.GONE else View.VISIBLE

        val rotation = if (isVisible) 0f else 180f
        arrowIcon.animate().rotation(rotation).setDuration(200).start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup basic xml
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

        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        // establish questions and answers
        val q1 = view.findViewById<LinearLayout>(R.id.faq_q1)
        val a1 = view.findViewById<TextView>(R.id.tv_answer_1)
        val arrow1 = view.findViewById<ImageView>(R.id.icon_arrow_1)
        q1.setOnClickListener { toggleFaq(a1, arrow1) }

        val q2 = view.findViewById<LinearLayout>(R.id.faq_q2)
        val a2 = view.findViewById<TextView>(R.id.tv_answer_2)
        val arrow2 = view.findViewById<ImageView>(R.id.icon_arrow_2)
        q2.setOnClickListener { toggleFaq(a2, arrow2) }

        val feedbackCard = view.findViewById<MaterialCardView>(R.id.card_send_feedback)
        val feedbackSection = view.findViewById<MaterialCardView>(R.id.feedback_section)
        feedbackCard.setOnClickListener {
            feedbackSection.visibility = View.VISIBLE
        }

        // send feedback option
        val feedbackInput = view.findViewById<EditText>(R.id.edit_feedback)
        val sendButton = view.findViewById<Button>(R.id.btn_send_feedback)

        sendButton.setOnClickListener {
            val message = feedbackInput.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Feedback can't be empty", Toast.LENGTH_SHORT).show()
            } else {
                // send feedback email via email app
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("aidantambling@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "User Feedback")
                    putExtra(Intent.EXTRA_TEXT, message)
                }

                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)
                    feedbackInput.setText("")
                    feedbackSection.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}