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

class identify_game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_identify_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val a_sign = findViewById<ImageButton>(R.id.a_sign)
        val p_sign = findViewById<ImageButton>(R.id.p_sign)
        val c_sign = findViewById<ImageButton>(R.id.c_sign)
        val b_sign = findViewById<ImageButton>(R.id.b_sign)
        var identify_bool = false



        val submitbtn = findViewById<Button>(R.id.submit_button)
        val returnbtn = findViewById<Button>(R.id.returnbtn)
        var selectedButton: ImageButton? = null

        val buttons = listOf(a_sign, p_sign, c_sign, b_sign)

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

                if(selectedButton == a_sign) {
                    a_sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                   identify_bool= true
//                    val intent = Intent(this, identify_game::class.java)
//
//                    startActivity(intent)
                }
                if (selectedButton != null && selectedButton != a_sign) {
                    selectedButton!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                  identify_bool = false

                }

            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
        returnbtn.setOnClickListener{
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("IDENTIFY_BOOL", identify_bool)
           startActivity(intent)

        }


    }


    }
