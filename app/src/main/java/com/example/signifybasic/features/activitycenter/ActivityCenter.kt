package com.example.signifybasic.features.activitycenter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.selecting_game

class ActivityCenter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_center)

        val activitySelect= findViewById<Button>(R.id.button2)
        val activityIdentify = findViewById<Button>(R.id.button3)
        activityIdentify.isActivated = false

        activitySelect.setOnClickListener{
            startActivity(Intent(this,selecting_game::class.java))
        }


    }
}