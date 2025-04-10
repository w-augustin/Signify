package com.example.signifybasic.features.tabs.resources

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signifybasic.R
import com.example.signifybasic.databinding.ResourcesActivityBinding
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ResourcesWrapper(
    val videos: List<ResourceItem>,
    val links: List<LinkItem>
)

class ResourcesFragment : Fragment() {

    private var _binding: ResourcesActivityBinding? = null
    private val binding get() = _binding!!

    private var showingAllVideos = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ResourcesActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        val jsonString = requireContext().assets.open("resources.json").bufferedReader().use { it.readText() }
        val wrapperType = object : TypeToken<ResourcesWrapper>() {}.type
        val resourcesWrapper: ResourcesWrapper = Gson().fromJson(jsonString, wrapperType)

        val allVideos = resourcesWrapper.videos
        val previewVideos = allVideos.take(4).toMutableList()

        val videoAdapter = ResourcesAdapter(previewVideos)
        binding.resourcesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.resourcesRecyclerView.adapter = videoAdapter

        binding.seeMoreButton.setOnClickListener {
            showingAllVideos = !showingAllVideos
            if (showingAllVideos) {
                videoAdapter.updateData(allVideos)
                binding.resourcesRecyclerView.post { binding.resourcesRecyclerView.requestLayout() }

                // Update the inner TextView manually
                binding.seeMoreButton.findViewById<TextView>(R.id.seeMoreText).text = "See Less"
            } else {
                videoAdapter.updateData(allVideos.take(4))
                binding.seeMoreButton.findViewById<TextView>(R.id.seeMoreText).text = "See More"
            }
        }

        val linkAdapter = LinksAdapter(resourcesWrapper.links)
        binding.linksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.linksRecyclerView.adapter = linkAdapter

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
