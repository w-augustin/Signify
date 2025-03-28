package com.example.signifybasic.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.MainActivity3
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.HomePage
import com.example.signifybasic.debug.DebugActivity
import com.example.signifybasic.database.DBHelper

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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dbHelper = DBHelper(this)

        // Check if the admin user already exists before inserting
        val adminUsername = "admin"
        if (!dbHelper.usernameExists(adminUsername)) {
            dbHelper.addUser(adminUsername, "admin@admin.com", "admin")
        }

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        startBtn = findViewById(R.id.start_btn)
        guestBtn = findViewById(R.id.guest_btn)
        debugBtn = findViewById(R.id.debugButton)

        // login button should route to the WelcomeCenter
        loginBtn.setOnClickListener {

            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            val dbHelper = DBHelper(this)

            if (dbHelper.isValidUser(username, password)) {
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

        // continue as guest should route to the WelcomeCenter - but for now, use to go to cameras
        guestBtn.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        debugBtn.setOnClickListener {
            val intent = Intent(this, DebugActivity::class.java)
            startActivity(intent)
        }
    }
}