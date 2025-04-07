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

class identify_game2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_identify_game2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val h_sign = findViewById<ImageButton>(R.id.h_sign)
        val m_sign = findViewById<ImageButton>(R.id.m_sign)
        val e_sign = findViewById<ImageButton>(R.id.e_sign)
        val k_sign = findViewById<ImageButton>(R.id.k_sign)
        var identify2_bool = false



        val submitbtn = findViewById<Button>(R.id.submit_button)
        val returnbtn = findViewById<Button>(R.id.returnbtn)
        var selectedButton: ImageButton? = null

        val buttons = listOf(h_sign, m_sign, e_sign, k_sign)

        fun resetButtons() {
            buttons.forEach { button ->
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

        }

        buttons.forEach { button ->
            button.setOnClickListener {
                resetButtons()
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
                selectedButton = button
            }
        }



        submitbtn.setOnClickListener {
            if (selectedButton != null) {
                resetButtons()

                if(selectedButton == h_sign) {
                    h_sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                    identify2_bool= true
//                    val intent = Intent(this, identify_game::class.java)
//
//                    startActivity(intent)
                }
                if (selectedButton != null && selectedButton != h_sign) {
                    selectedButton!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                    identify2_bool = false

                }

            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
        returnbtn.setOnClickListener{
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("IDENTIFY2_BOOL", identify2_bool)
            startActivity(intent)

        }

    }
}