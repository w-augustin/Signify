package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Find buttons by ID
        val btnAccount = findViewById<Button>(R.id.btn_account)
        val btnPreferences = findViewById<Button>(R.id.btn_preferences)
        val btnNotifications = findViewById<Button>(R.id.btn_notifications)
        val btnHelp = findViewById<Button>(R.id.btn_help)

//        btnAccount.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    this@SettingsActivity,
//                    AccountActivity::class.java
//                )
//            )
//        }
//        btnPreferences.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    this@SettingsActivity,
//                    PreferencesActivity::class.java
//                )
//            )
//        }
//        btnNotifications.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    this@SettingsActivity,
//                    NotificationsActivity::class.java
//                )
//            )
//        }
//        btnHelp.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    this@SettingsActivity,
//                    HelpActivity::class.java
//                )
//            )
//        }
    }
}