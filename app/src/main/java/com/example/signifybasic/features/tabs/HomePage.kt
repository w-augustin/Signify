package com.example.signifybasic.features.tabs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.achievements.AchievementsFragment
import com.example.signifybasic.features.tabs.home.HomeFragment
import com.example.signifybasic.features.tabs.profile.ProfileFragment
import com.example.signifybasic.features.tabs.notifications.NotificationsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.signifybasic.signrecognition.MainActivity3

class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_center)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(HomeFragment())
                    true
                }
                R.id.nav_achievements -> {
                    switchFragment(AchievementsFragment())
                    true
                }
                R.id.nav_notifications -> {
                    switchFragment(NotificationsFragment())
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

    }
        private fun switchFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
}
