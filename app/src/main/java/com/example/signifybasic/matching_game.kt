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

class matching_game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_matching_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val p_sign = findViewById<ImageButton>(R.id.p_sign)
        val a_sign = findViewById<ImageButton>(R.id.a_sign)
        val c_sign = findViewById<ImageButton>(R.id.c_sign)
        val b_sign = findViewById<ImageButton>(R.id.b_sign)

        val p_option = findViewById<Button>(R.id.p_option)
        val a_option = findViewById<Button>(R.id.a_option)
        val c_option = findViewById<Button>(R.id.c_option)
        val b_option = findViewById<Button>(R.id.b_option)
        val continueButton= findViewById<Button>(R.id.continue_button)

        var btnSelected = false // check if btn been selected
        var lastClickedSign: ImageButton? = null //  track the last clicked sign
        var lastClickedOption: Button? = null // track the last clicked option
        var matching_bool =false

        var matchedCount = 0
        val totalMatches = 4

        val signs = listOf(p_sign, a_sign, c_sign, b_sign)
        val options = listOf(p_option, a_option, c_option, b_option)

        for (i in signs.indices) {
            signs[i].setOnClickListener {
                if (!btnSelected) {
                    lastClickedSign = signs[i]
                    signs[i].backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
                    btnSelected = true
                }
            }
        }

        for (i in options.indices) { // need to click sign first then option
            options[i].setOnClickListener {
                if (btnSelected && lastClickedSign != null) {
                    lastClickedOption = options[i]
                    options[i].backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)

                    if (lastClickedSign!!.contentDescription == lastClickedOption!!.text) {
                        // If they match increase count and mark green
                        lastClickedSign!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                        lastClickedOption!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                        matchedCount++
                    } else {
                        // If no match, mark red
                        lastClickedSign!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                        lastClickedOption!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                    }
                    //ensure 2 btns are selected at a time
                    btnSelected = false
                }
            }
        }
        continueButton.setOnClickListener {
            if (matchedCount == totalMatches) {
                Toast.makeText(this, "You've matched them all correctly", Toast.LENGTH_SHORT).show()
                matching_bool = true

                val intent = Intent(this, ActivityCenter::class.java)
                intent.putExtra("MATCHING_BOOL", matching_bool)
                startActivity(intent)
                finish() // optional: so user can’t return to completed game
            } else {
                Toast.makeText(this, "Try again, you got $matchedCount out of $totalMatches right", Toast.LENGTH_SHORT).show()
            }

            // Disable all buttons
            signs.forEach { it.isEnabled = false }
            options.forEach { it.isEnabled = false }
        }

        val resetButton = findViewById<Button>(R.id.reset_btn)
        resetButton.setOnClickListener { // reset the entire game
            for(sign in signs){
                sign.isEnabled =true
                sign.backgroundTintList = null
            }
            for(option in options){
                option.isEnabled = true
                option.backgroundTintList = getColorStateList(R.color.primary_blue)
            }
            btnSelected = false
            lastClickedSign = null
            lastClickedOption = null
            matchedCount = 0
        }

        val returnBtn = findViewById<Button>(R.id.return_btn)
        returnBtn.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("MATCHING_BOOL", matching_bool)
            startActivity(intent)
        }


    }
}



