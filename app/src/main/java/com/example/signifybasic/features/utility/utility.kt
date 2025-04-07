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

fun getScaledTextSize(context: Context): Float {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return when (prefs.getString("text_size", "medium")) {
        "small" -> 12f
        "medium" -> 14f
        "large" -> 18f
        else -> 14f
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


