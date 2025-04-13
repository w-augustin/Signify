package com.example.signifybasic.features.utility

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R
import com.google.android.material.appbar.MaterialToolbar
import com.example.signifybasic.features.tabs.achievements.AchievementMeta
import org.json.JSONArray

fun loadAllAchievementsFromAssets(context: Context): List<AchievementMeta> {
    val inputStream = context.assets.open("valid_achievements.json")
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    val jsonArray = JSONArray(jsonString)
    val list = mutableListOf<AchievementMeta>()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val name = obj.getString("name")
        val iconName = obj.getString("icon")
        val description = obj.getString("description")
        val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        list.add(AchievementMeta(name, resId, description))
    }

    return list
}


fun getScaledTextSize(context: Context): Float {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return when (prefs.getString("text_size", "medium")) {
        "small" -> 16f
        "medium" -> 20f
        "large" -> 22f
        else -> 20f
    }
}

fun applyTextSizeToAllTextViews(root: View, context: Context) {
    val textSize = getScaledTextSize(context)
    if (root is MaterialToolbar) return

    if (root is TextView) {
        root.textSize = textSize
    } else if (root is ViewGroup) {
        for (i in 0 until root.childCount) {
            applyTextSizeToAllTextViews(root.getChildAt(i), context)
        }
    }
}
fun isHighContrastEnabled(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getBoolean("high_contrast_enabled", false)
}

fun applyHighContrastToAllViews(root: View, context: Context) {
    if (root is MaterialToolbar) return // Skip toolbar

    if (root is TextView) {
        root.setTextColor(ContextCompat.getColor(context, android.R.color.black))
    }

    if (root is ViewGroup) {
        for (i in 0 until root.childCount) {
            applyHighContrastToAllViews(root.getChildAt(i), context)
        }
    }
}

fun removeHighContrastFromAllViews(root: View, context: Context) {
//    if (root is MaterialToolbar) return
//
//    if (root is TextView) {
//        root.setTextColor(ContextCompat.getColor(context, android.R.color.black))
//    }
//
//    if (root is CardView) {
//        root.setCardBackgroundColor(profile)
//    }
//
//    if (root is ViewGroup) {
//        for (i in 0 until root.childCount) {
//            removeHighContrastFromAllViews(root.getChildAt(i), context)
//        }
//    }
}


