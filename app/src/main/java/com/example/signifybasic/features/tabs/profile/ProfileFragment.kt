package com.example.signifybasic.features.tabs.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.databinding.FragmentProfileBinding
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled

class ProfileFragment : Fragment() {
    lateinit var loginBtn : Button

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        //Retrieve username to assign text
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null)
        val email = sharedPref.getString("userEmail", null)

        val dbHelper = DBHelper(requireContext())
        val userId = username?.let { dbHelper.getUserIdByUsername(it) }

        val expPoints = userId?.let { dbHelper.getUserTotalExp(it) } ?: 0

        view.findViewById<TextView>(R.id.exptextview)?.text = "$expPoints EXP"

        val knownWords = userId?.let { dbHelper.getKnownWordCount(it) } ?: 0
        view.findViewById<TextView>(R.id.wordsKnownTextView)?.text = "$knownWords words"

        val currentModule = userId?.let { dbHelper.getCurrentModuleTitle(it) } ?: "None"
        view.findViewById<TextView>(R.id.currentModuleTextView)?.text = currentModule


        binding.userDisplay.text = username
        binding.emailDisplay.text = email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
