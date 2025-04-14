package com.example.signifybasic.features.tabs.profile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.databinding.FragmentProfileBinding
import com.example.signifybasic.features.tabs.achievements.AchievementGridAdapter
import com.example.signifybasic.features.tabs.achievements.AchievementItem
import com.example.signifybasic.features.tabs.achievements.AchievementMeta
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.example.signifybasic.features.utility.loadAllAchievementsFromAssets

class ProfileFragment : Fragment() {

    private fun showBadgePickerDialog(
        userId: Int,
        slot: Int,
        unlocked: List<AchievementMeta>,
        targetView: ImageView,
        dbHelper: DBHelper
    ) {
        val currentBadges = dbHelper.getUserBadges(userId)

        // Filter out badges already used in other slots
        val alreadyUsed = currentBadges.filterKeys { it != slot }.values.toSet()
        val available = unlocked.filter { it.name !in alreadyUsed }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_achievement_picker, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.achievementGrid)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = AchievementGridAdapter(
            available.map { AchievementItem(it.name, it.iconResId, true) }
        ) { selected ->
            targetView.setImageResource(selected.iconResId)
            dbHelper.setUserBadge(userId, slot, selected.name)
            (dialogView.parent as? ViewGroup)?.let { (it.parent as? AlertDialog)?.dismiss() }
        }

        dialogView.findViewById<Button>(R.id.clearButton)?.setOnClickListener {
            dbHelper.clearUserBadge(userId, slot)
            targetView.setImageResource(R.drawable.checkmark) // Reset to default
            (dialogView.parent as? ViewGroup)?.let { (it.parent as? AlertDialog)?.dismiss() }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Badge")
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }


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

        val safeUsername = username ?: "0"
        val xpPoints = dbHelper.getUserProgress(safeUsername)


        view.findViewById<TextView>(R.id.exptextview)?.text = "$xpPoints EXP"

        val knownWords = userId?.let { dbHelper.getKnownWordCount(it) } ?: 0
        view.findViewById<TextView>(R.id.wordsKnownTextView)?.text = "$knownWords words"

        val currentModule = userId?.let { dbHelper.getCurrentModuleTitle(it) } ?: "None"
        view.findViewById<TextView>(R.id.currentModuleTextView)?.text = currentModule


        val badgeViews = listOf(
            view.findViewById<ImageView>(R.id.badgeSlot1),
            view.findViewById<ImageView>(R.id.badgeSlot2),
            view.findViewById<ImageView>(R.id.badgeSlot3)
        )

        val unlocked = userId?.let { dbHelper.getAchievements(it) } ?: listOf()
        val all = loadAllAchievementsFromAssets(requireContext())
        val unlockedItems = all.filter { unlocked.contains(it.name) }

        val userBadges = userId?.let { dbHelper.getUserBadges(it) } ?: emptyMap()

        // Populate initially
        badgeViews.forEachIndexed { i, badgeView ->
            val meta = all.find { it.name == userBadges[i] }
            if (meta != null) badgeView.setImageResource(meta.iconResId)

            badgeView.setOnClickListener {
                showBadgePickerDialog(userId ?: return@setOnClickListener, i, unlockedItems, badgeView, dbHelper)
            }
        }

        binding.userDisplay.text = username
        binding.emailDisplay.text = email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
