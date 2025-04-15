package com.example.signifybasic.signrecognition

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.signrecognition.videorecognition.RecordVideoActivity

class MainActivity3 : AppCompatActivity() {

    private lateinit var recordVideoButton: Button
    private lateinit var loadVideoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        recordVideoButton = findViewById(R.id.record_video_button)
        loadVideoButton = findViewById(R.id.load_video_button)

        // Handle recording video
        recordVideoButton.setOnClickListener {
            val intent = Intent(this, RecordVideoActivity::class.java)
            startActivity(intent)
        }

        /* Handle loading image by user ID
        loadVideoButton.setOnClickListener {
            val intent = Intent(this, LoadImageActivity::class.java)
            startActivity(intent)
        }*/
    }


}
