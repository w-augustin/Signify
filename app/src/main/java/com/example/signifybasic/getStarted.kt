package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.activitycenter.ActivityCenter

class getStarted : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)

        val nextBtn = findViewById<Button>(R.id.nextButton)
        nextBtn.setOnClickListener {
           startActivity(Intent(this,ActivityCenter::class.java ))
        }

    }
}