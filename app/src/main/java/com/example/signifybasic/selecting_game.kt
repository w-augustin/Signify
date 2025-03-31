package com.example.signifybasic

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.example.signifybasic.features.activitycenter.ActivityCenter


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
        val returnBtn=findViewById<Button>(R.id.returnbtn)
        val correctButton = findViewById<Button>(R.id.option2)
        val incorrectButton = findViewById<Button>(R.id.option1)
        val incorrectButton3 = findViewById<Button>(R.id.option3)
        val incorrectButton4 = findViewById<Button>(R.id.option4)
        val submitButton= findViewById<Button>(R.id.submit_button)

        var isCorrect = false

        var selectedButton: Button? = null

        val buttons = listOf(correctButton, incorrectButton, incorrectButton3, incorrectButton4)

        fun resetButtons() {
            buttons.forEach { button ->
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                }

        }

        buttons.forEach { button ->
            button.setOnClickListener {
                resetButtons()
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                selectedButton = button
            }
        }

        submitButton.setOnClickListener {
            if (selectedButton != null) {
                resetButtons()

                if(selectedButton == correctButton) {
                    correctButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                    isCorrect = true
                   // val intent = Intent(this, identify_game::class.java)
                    //startActivity(intent)
                }
                if (selectedButton != null && selectedButton != correctButton) {
                    selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                }

            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
        returnBtn.setOnClickListener{
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("IS_CORRECT", isCorrect)
            startActivity(intent)

        }


    }
}
