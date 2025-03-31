package com.example.signifybasic.features.tabs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AchievementsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

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