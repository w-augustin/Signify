package com.example.signifybasic.features.tabs.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.signifybasic.R
import com.example.signifybasic.databinding.FragmentAchievementsBinding
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }
//
//        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
//        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
//            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
//            v.setPadding(0, topInset, 0, 0)
//            insets
//        }
//
//        toolbar.setNavigationOnClickListener {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//        binding.textAchievements.text = "🏆 You have 5 new badges!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
