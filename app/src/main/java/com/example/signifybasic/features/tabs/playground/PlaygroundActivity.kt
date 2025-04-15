package com.example.signifybasic.features.tabs.playground

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.playground.liverecognition.LiveSignRecognitionActivity
import com.example.signifybasic.features.tabs.playground.videorecognition.RecordVideoActivity
import com.google.android.material.card.MaterialCardView

class PlaygroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground)

        findViewById<MaterialCardView>(R.id.cardLiveCamera).setOnClickListener {
            startActivity(Intent(this, LiveSignRecognitionActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardRecording).setOnClickListener {
            startActivity(Intent(this, RecordVideoActivity::class.java))
        }
    }
}
