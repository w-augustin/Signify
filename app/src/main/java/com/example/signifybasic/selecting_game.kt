package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.activitycenter.ActivityCenter

class selecting_game : AppCompatActivity() {

    private lateinit var correctButton: Button
    private lateinit var submitButton: Button
    private var selectedButton: Button? = null
    private var isCorrect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecting_game)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        val returnBtn = findViewById<Button>(R.id.returnbtn)
        correctButton = findViewById(R.id.option2)
        val incorrectButtons = listOf(
            findViewById<Button>(R.id.option1),
            findViewById<Button>(R.id.option3),
            findViewById<Button>(R.id.option4)
        )
        submitButton = findViewById(R.id.submit_button)

        val allButtons = incorrectButtons + correctButton

        fun resetButtonStyles() {
            allButtons.forEach {
                it.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                it.isEnabled = true
            }
        }

        // Selection logic
        allButtons.forEach { button ->
            button.setOnClickListener {
                resetButtonStyles()
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                selectedButton = button
            }
        }

        // Submit/Continue logic
        submitButton.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetButtonStyles()

            if (selectedButton == correctButton) {
                isCorrect = true
                correctButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                allButtons.forEach { it.isEnabled = false }
                submitButton.text = "Continue"

                submitButton.setOnClickListener {
                    val intent = Intent(this, identify_game::class.java)
                    intent.putExtra("SELECTING_BOOL", true) // optional if identify_game needs it
                    startActivity(intent)
                    finish() // optional: prevents coming back to this screen with back button
                }
            } else {
                selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()
                selectedButton = null
            }
        }

        returnBtn.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("IS_CORRECT", isCorrect)
            startActivity(intent)
            finish()
        }
    }
}

