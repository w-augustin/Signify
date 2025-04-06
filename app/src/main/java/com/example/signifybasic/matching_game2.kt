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
import com.example.signifybasic.R.drawable.*
import com.example.signifybasic.R.id.*
import com.example.signifybasic.features.activitycenter.ActivityCenter


class matching_game2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_matching_game2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val u_sign = findViewById<ImageButton>(R.id.u_sign)
        val v_sign = findViewById<ImageButton>(R.id.v_sign)
        val x_sign = findViewById<ImageButton>(R.id.x_sign)
        val w_sign = findViewById<ImageButton>(R.id.w_sign)


        val u_option = findViewById<Button>(R.id.u_option)
        val v_option = findViewById<Button>(R.id.v_option)
        val x_option = findViewById<Button>(R.id.x_option)
        val w_option = findViewById<Button>(R.id.w_option)
        val continueButton= findViewById<Button>(R.id.continue_button)
        var matching2_bool=false

        var btnSelected = false // check if btn been selected
        var lastClickedSign: ImageButton? = null //  track the last clicked sign
        var lastClickedOption: Button? = null // track the last clicked option

        var matchedCount = 0
        val totalMatches = 4

        val signs = listOf(u_sign, v_sign, x_sign, w_sign)
        val options = listOf(u_option, v_option, x_option, w_option)

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
            if(matchedCount == totalMatches){
                Toast.makeText(this, "Youve matched all them all correctly", Toast.LENGTH_SHORT).show()
               matching2_bool =true
                //startActivity(Intent())
            }else{
                Toast.makeText(this,"Try again, you got $matchedCount out of $totalMatches right",
                    Toast.LENGTH_SHORT
                ).show()
            }
            u_sign.isEnabled = false
            v_sign.isEnabled = false
            x_sign.isEnabled = false
            w_sign.isEnabled = false

            u_option.isEnabled = false
            v_option.isEnabled = false
            x_option.isEnabled = false
            w_option.isEnabled = false
        }

        val resetButton = findViewById<Button>(R.id.reset_button)
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

        val returnBtn = findViewById<Button>(R.id.return_button)
        returnBtn.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            intent.putExtra("MATCHING2_BOOL", matching2_bool)
            startActivity(intent)        }


    }
}



