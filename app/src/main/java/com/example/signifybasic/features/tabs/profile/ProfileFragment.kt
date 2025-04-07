package com.example.signifybasic.features.tabs.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
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

        var username = requireActivity().intent.getStringExtra("Username") ?: "Guest"
        var password = requireActivity().intent.getStringExtra("Password") ?: "Unknown"
        password = "Unknown"
        username = "Unknown"

        Log.d("username: ", username)
        Log.d("pw: ", password)

        binding.userDisplay.text = username
        binding.passDisplay.text = password
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
