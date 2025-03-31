package com.example.signifybasic.features.tabs.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.signifybasic.databinding.FragmentProfileBinding

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

        var username = requireActivity().intent.getStringExtra("Username") ?: "Guest"
        var password = requireActivity().intent.getStringExtra("Password") ?: "Unknown"
        password = "Unknown"
        username = "Unknown"

        Log.d("username: ", username)
        Log.d("pw: ", password)

        binding.userDisplay.text = username
        binding.passDisplay.text = password

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
