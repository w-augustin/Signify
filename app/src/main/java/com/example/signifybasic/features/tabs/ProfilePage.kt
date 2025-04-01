package com.example.signifybasic.features.tabs

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.R

class ProfilePage : AppCompatActivity() {
    lateinit var loginBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginBtn = findViewById(R.id.back_btn)
        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")
        val userView = findViewById<View>(R.id.user_display) as TextView
        userView.text = username
        val passView = findViewById<View>(R.id.pass_display) as TextView
        passView.text = password

        loginBtn.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)

            finish()
//                Log.i("Test Credentials", "Username: $username and Password: $password")
        }
    }
}