package com.example.signifybasic.features.tabs.resources

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signifybasic.databinding.ResourcesActivityBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ResourcesWrapper(
    val videos: List<ResourceItem>,
    val links: List<LinkItem>
)

class ResourcesPage : AppCompatActivity() {

    private lateinit var binding: ResourcesActivityBinding
    private var showingAllVideos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResourcesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val jsonString = assets.open("resources.json").bufferedReader().use { it.readText() }
        val wrapperType = object : TypeToken<ResourcesWrapper>() {}.type
        val resourcesWrapper: ResourcesWrapper = Gson().fromJson(jsonString, wrapperType)

        val allVideos = resourcesWrapper.videos
        val previewVideos = allVideos.take(4).toMutableList()

        val videoAdapter = ResourcesAdapter(previewVideos.toMutableList())
        binding.resourcesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.resourcesRecyclerView.adapter = videoAdapter

        binding.seeMoreButton.setOnClickListener {
            showingAllVideos = !showingAllVideos
            if (showingAllVideos) {
                Log.d("ResourcesPage", "allVideos.size = ${allVideos.size}")
                videoAdapter.updateData(allVideos)
                binding.resourcesRecyclerView.post {
                    binding.resourcesRecyclerView.requestLayout()
                }
                binding.seeMoreButton.text = "See Less"
            } else {
                videoAdapter.updateData(allVideos.take(4))
                binding.seeMoreButton.text = "See More"
            }
        }

        val linkAdapter = LinksAdapter(resourcesWrapper.links)
        binding.linksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.linksRecyclerView.adapter = linkAdapter
    }
}
