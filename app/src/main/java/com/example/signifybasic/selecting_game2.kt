package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.activitycenter.ActivityCenter

class selecting_game2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selecting_game2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val returnBtn=findViewById<Button>(R.id.return_button)
        val correctButton = findViewById<Button>(R.id.w_option)
        val incorrectButton = findViewById<Button>(R.id.f_option)
        val incorrectButton3 = findViewById<Button>(R.id.z_option)
        val incorrectButton4 = findViewById<Button>(R.id.m_option)
        val submitButton= findViewById<Button>(R.id.submit_button)

        var selecting2_bool = false

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
                    selecting2_bool = true
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
            intent.putExtra("SELECTING2_BOOL", selecting2_bool)
            startActivity(intent)

        }

    }
}