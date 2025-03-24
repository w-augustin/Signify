package com.example.signifybasic.features.tabs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.signifybasic.MainActivity3
import com.example.signifybasic.R
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.tabs.discussion.DiscussionPage
import com.example.signifybasic.features.tabs.resources.ResourcesPage
import com.example.signifybasic.features.tabs.settings.SettingsPage
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_center)

        val cardSettings = findViewById<CardView>(R.id.card_settings)
        val cardDiscussion = findViewById<CardView>(R.id.card_discussion)
        val cardActivityCenter = findViewById<CardView>(R.id.card_activity_center)
        val cardResources = findViewById<CardView>(R.id.card_resources)
        val cardPlayground = findViewById<CardView>(R.id.card_playground)

        cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsPage::class.java))
        }

        cardDiscussion.setOnClickListener {
            startActivity(Intent(this, DiscussionPage::class.java))
        }

        cardActivityCenter.setOnClickListener {
            startActivity(Intent(this, ActivityCenter::class.java))
        }

        cardResources.setOnClickListener {
            startActivity(Intent(this, ResourcesPage::class.java))
        }

        cardPlayground.setOnClickListener {
            startActivity(Intent(this, MainActivity3::class.java))
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.nav_home -> Intent(this, HomePage::class.java)
                R.id.nav_achievements -> Intent(this, AchievementsPage::class.java)
                R.id.nav_notifications -> Intent(this, NotificationsPage::class.java)
                R.id.nav_profile -> Intent(this, ProfilePage::class.java)
                else -> null
            }

            intent?.let { startActivity(it) }
            true
        }
    }
}
