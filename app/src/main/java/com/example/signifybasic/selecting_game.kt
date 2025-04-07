package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.base.BaseGameActivity

class selecting_game : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_selecting_game

    private lateinit var correctButton: Button
    private lateinit var submitButton: Button
    private var selectedButton: Button? = null
    private var isCorrect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBar.progress = 14

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

        allButtons.forEach { button ->
            button.setOnClickListener {
                resetButtonStyles()
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                selectedButton = button
            }
        }

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
                    intent.putExtra("SELECTING_BOOL", true)
                    startActivity(intent)
                    finish()
                }
            } else {
                selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()
                selectedButton = null
            }
        }
    }
}

