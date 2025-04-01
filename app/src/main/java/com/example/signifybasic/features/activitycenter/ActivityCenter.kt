package com.example.signifybasic.features.activitycenter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.identify_game
import com.example.signifybasic.selecting_game

class ActivityCenter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_center)

        val activitySelect= findViewById<Button>(R.id.button2)
        val activityIdentify = findViewById<Button>(R.id.button3)
        activityIdentify.isActivated = false

        var currProgress: Int =0;
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val isCorrect = intent.getBooleanExtra("IS_CORRECT", false)
        val identify_bool =intent.getBooleanExtra("IDENTIFY_BOOL", false)
        progressBar.max = 100

        fun increaseProgressBar() {
            currProgress = currProgress + 20

            if(currProgress > progressBar.max){
                currProgress = progressBar.max
            }
            progressBar.setProgress(currProgress)

        }

        activitySelect.setOnClickListener{
                startActivity(Intent(this, selecting_game::class.java))
            }

            if(isCorrect){
               increaseProgressBar()
                activityIdentify.isActivated = true
                activityIdentify.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))

                activityIdentify.setOnClickListener {
                    startActivity( Intent(this,identify_game::class.java))
                    increaseProgressBar()

                }
            }

        if(identify_bool){
            // need to find a way to loop this or update after
            increaseProgressBar()
            increaseProgressBar()

                // need to create a function that updates the ui
            activityIdentify.isActivated = true
            activityIdentify.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))

            activityIdentify.setOnClickListener {
                startActivity( Intent(this,identify_game::class.java))
                increaseProgressBar()

            }
        }

    }
}
