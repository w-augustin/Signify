package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class SignupPage : AppCompatActivity() {

    lateinit var usernameInput : EditText
    lateinit var passwordInput : EditText
    lateinit var signupButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        usernameInput = findViewById(R.id.username_input)
//        passwordInput = findViewById(R.id.password_input)
//        loginBtn = findViewById(R.id.login_btn)
//        startBtn = findViewById(R.id.start_btn)
//        guestBtn = findViewById(R.id.guest_btn)
        signupButton = findViewById(R.id.signup_button)

        // once user signs up, they should be routed to the 'test' page
        signupButton.setOnClickListener {
//            val username = usernameInput.text.toString()
//            val password = passwordInput.text.toString()

            val intent = Intent(this, TestPage::class.java)
//            intent.putExtra("Username", username)
//            intent.putExtra("Password", password)
            startActivity(intent)
        }
    }
}