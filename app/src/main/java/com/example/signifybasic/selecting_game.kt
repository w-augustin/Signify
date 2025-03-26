package com.example.signifybasic

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class selecting_game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selecting_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val correctButton = findViewById<Button>(R.id.option2)
        val incorrectButton = findViewById<Button>(R.id.option1)
        val incorrectButton3 = findViewById<Button>(R.id.option3)
        val incorrectButton4 = findViewById<Button>(R.id.option4)

        // Keep track of the selected button
        var selectedButton: Button? = null

        // List of all button options
        val buttons = listOf(correctButton, incorrectButton, incorrectButton3, incorrectButton4)

        fun resetIncorrectButtons() {
            // Reset all incorrect buttons to red
            buttons.forEach { button ->
                if (button != correctButton) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                }
            }
        }

        buttons.forEach { button ->
            button.setOnClickListener {
                // call function
                resetIncorrectButtons()

                // If the correct button was selected, set it to green
                if (button == correctButton) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                }
                // Keep track of the selected button
                selectedButton = button
            }
        }

        if(selectedButton == correctButton){

        }





    }
}
