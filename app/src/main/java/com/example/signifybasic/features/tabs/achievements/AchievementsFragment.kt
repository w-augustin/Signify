package com.example.signifybasic.features.tabs.achievements

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.databinding.FragmentAchievementsBinding
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import androidx.recyclerview.widget.GridLayoutManager
import com.example.signifybasic.features.utility.loadAllAchievementsFromAssets
import com.google.android.material.appbar.MaterialToolbar

data class AchievementItem(
    val name: String, // achievement name
    val iconResId: Int, // achievement icon
    val isUnlocked: Boolean
)

data class AchievementMeta(
    val name: String,
    val iconResId: Int,
    val description: String
)

class AchievementGridAdapter(
    private val achievements: List<AchievementItem>,
    private val onClick: (AchievementItem) -> Unit
) : RecyclerView.Adapter<AchievementGridAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.achievementIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = achievements[position]
        holder.icon.setImageResource(item.iconResId)

        if (item.isUnlocked) {
            ImageViewCompat.setImageTintList(holder.icon, null) // show original colors
        } else {
            ImageViewCompat.setImageTintList(holder.icon, ColorStateList.valueOf(Color.BLACK)) // dim
        }


        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }



    override fun getItemCount() = achievements.size
}

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    // We'll load this from assets in onViewCreated
    private lateinit var allAchievements: List<AchievementMeta>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Load from assets/valid_achievements.json
//    fun loadAllAchievementsFromAssets(context: Context): List<AchievementMeta> {
//        val inputStream = context.assets.open("valid_achievements.json")
//        val jsonString = inputStream.bufferedReader().use { it.readText() }
//
//        val jsonArray = org.json.JSONArray(jsonString)
//        val list = mutableListOf<AchievementMeta>()
//
//        for (i in 0 until jsonArray.length()) {
//            val obj = jsonArray.getJSONObject(i)
//            val name = obj.getString("name")
//            val iconName = obj.getString("icon")
//            val description = obj.getString("description")
//            val resId = mapToResId(context, iconName)
//            list.add(AchievementMeta(name, resId, description))
//        }
//
//        return list
//    }

    private fun mapToResId(context: Context, iconName: String): Int {
        return context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    private fun mapAchievementsToIcons(unlockedNames: List<String>): List<AchievementItem> {
        val normalizedUnlocked = unlockedNames.map { it.trim().lowercase() }

        return allAchievements.map { meta ->
            val isUnlocked = normalizedUnlocked.contains(meta.name.trim().lowercase())
            AchievementItem(
                name = meta.name,
                iconResId = meta.iconResId,
                isUnlocked = isUnlocked
            )
        }.sortedByDescending { it.isUnlocked }
    }


    private fun refreshAchievements(userID: Int) {
        val dbHelper = DBHelper(requireContext())
        val unlocked = dbHelper.getAchievements(userID)
        val updatedItems = mapAchievementsToIcons(unlocked)
        binding.achievementsRecyclerView.adapter =
            AchievementGridAdapter(updatedItems) { item -> showAchievementDialog(item, dbHelper, userID) }
    }

    private fun showAchievementDialog(item: AchievementItem, dbHelper: DBHelper, userID: Int) {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_achievement, null)

        val titleView = dialogView.findViewById<TextView>(R.id.dialog_achievement_title)
        val descView = dialogView.findViewById<TextView>(R.id.dialog_achievement_description)
        val addButton = dialogView.findViewById<Button>(R.id.dialog_add_button)

        titleView.text = item.name
        val meta = allAchievements.find { it.name.trim().equals(item.name.trim(), ignoreCase = true) }
        descView.text = meta?.description ?: "No description available."

        // 👇 Log just to confirm it’s inflating correctly
        Log.d("ACHIEVEMENT", "Dialog shown for: ${item.name}")

        addButton.setOnClickListener {
            val added = dbHelper.addAchievement(userID, item.name, requireContext())
            if (added){
                refreshAchievements(userID)
                Log.d("ACHIEVEMENT", "Add result: $added")
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null)

        val textView = view.findViewById<TextView>(R.id.header_title)
        val originalText = textView.text.toString()
        textView.text = originalText.replace("{User}", username ?: "Guest")

        val dbHelper = DBHelper(requireContext())
        val recyclerView = view.findViewById<RecyclerView>(R.id.achievementsRecyclerView)
        val userID = username?.let { dbHelper.getUserIdByUsername(it) } ?: return

        // Load valid achievement definitions
        allAchievements = loadAllAchievementsFromAssets(requireContext())

        // Get user's unlocked achievements from the database
        val unlockedAchievementNames = dbHelper.getAchievements(userID)
        Log.d("ACHIEVEMENT", "Loaded userID: $userID")
        Log.d("ACHIEVEMENT", unlockedAchievementNames.toString())

        // Combine both into what the adapter will use
        val achievementItems = mapAchievementsToIcons(unlockedAchievementNames)

        // Show in RecyclerView
        val adapter = AchievementGridAdapter(achievementItems) { item ->
            showAchievementDialog(item, dbHelper, userID)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = adapter

        binding.buttonClearAchievements.setOnClickListener {
            val userId = username?.let { dbHelper.getUserIdByUsername(it) }

            if (userId != null) {
                val cleared = dbHelper.clearAchievementsForUser(userId)
                if (cleared) {
                    Toast.makeText(context, "Achievements cleared!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No achievements to clear.", Toast.LENGTH_SHORT).show()
                }

                // Force refresh
                val unlocked = dbHelper.getAchievements(userId)
                val updatedItems = mapAchievementsToIcons(unlocked)
                binding.achievementsRecyclerView.adapter =
                    AchievementGridAdapter(updatedItems) { item -> showAchievementDialog(item, dbHelper, userID) }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

