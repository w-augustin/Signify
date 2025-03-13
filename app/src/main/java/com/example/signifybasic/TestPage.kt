package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class TestPage : AppCompatActivity() {

    private lateinit var continueButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.test_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.test)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // temporary implementation : just continue to welcome center.
        continueButton = findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            val intent = Intent(this, WelcomeCenter::class.java)
            startActivity(intent)
        }
    }
}