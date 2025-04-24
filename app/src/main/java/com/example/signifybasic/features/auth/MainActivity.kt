package com.example.signifybasic.features.auth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.debug.DebugActivity
import com.example.signifybasic.features.tabs.HomePage


class MainActivity : AppCompatActivity() {
    // landing page (where you start when you launch the app)

    private lateinit var usernameInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var loginBtn : Button
    private lateinit var startBtn : Button
    private lateinit var guestBtn : Button
    private lateinit var debugBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //this.deleteDatabase("SignifyDB")

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val dbHelper = DBHelper(applicationContext)

        // Check if the admin user already exists before inserting
        val adminUsername = "admin"
        if (!dbHelper.usernameExists(adminUsername)) {
            dbHelper.addUser(adminUsername, "admin", "admin@admin.com")
        }

        // establish notification channel
        val userId = adminUsername?.let { dbHelper.getUserIdByUsername(it) }
        val safeuserid = userId ?: 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for daily reminders"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        dbHelper.insertUserSettings(safeuserid)

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        startBtn = findViewById(R.id.start_btn)
        guestBtn = findViewById(R.id.guest_btn)
        debugBtn = findViewById(R.id.debug_button)

        debugBtn.setOnClickListener{
            startActivity(Intent(this, DebugActivity::class.java))
        }

        // login button should route to the WelcomeCenter
        loginBtn.setOnClickListener {

            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (dbHelper.isValidUser(username, password)) {

                // Store the logged-in username in SharedPreferences
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("loggedInUser", username) // Save username

                // retrieve + save email
                val email = dbHelper.getEmailByUsername(username)
                editor.putString("userEmail", email)

                // store logged-in password
                editor.putString("userPassword", password) // save password
                editor.apply()

                val loginUserId = dbHelper.getUserIdByUsername(username)
                if (loginUserId != null) {
                    dbHelper.recordLoginDate(loginUserId)
                }

                // update login streak according to user's login history
                val streak = loginUserId?.let { it1 -> dbHelper.getLoginStreak(it1) }
                if (streak != null) {
                    if (streak == 1) {
                        dbHelper.addAchievement(loginUserId, "First Login", this)
                    }
                    if (streak >= 3) {
                        dbHelper.addAchievement(loginUserId, "Signify Streak", this)
                    }
                    if (streak >= 7) {
                        dbHelper.addAchievement(loginUserId, "Signify Streak", this)
                    }
                }

                // navigate to homepage
                val intent = Intent(this, HomePage::class.java)
                startActivity(intent)
            } else {
                // Failed login error message
                Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
            }


        }

        // get started button should route to the signup page
        startBtn.setOnClickListener {
            val intent = Intent(this, SignupPage::class.java)
            startActivity(intent)
        }

        // continue as guest should route to the home page straight away
        guestBtn.setOnClickListener {
            // User will be listed as "Guest"
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("loggedInUser", "Guest")
            editor.apply()

            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        //Retrieve username
        //val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        //val username = sharedPref.getString("loggedInUser", null) // Retrieve username

        //Clear username
        //val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        //val editor = sharedPref.edit()
        //editor.remove("loggedInUser")
        //editor.apply()

    }
}