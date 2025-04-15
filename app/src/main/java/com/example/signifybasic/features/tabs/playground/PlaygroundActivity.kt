package com.example.signifybasic.features.tabs.playground

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.playground.liverecognition.LiveSignRecognitionActivity
import com.example.signifybasic.features.tabs.playground.videorecognition.RecordVideoActivity
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView

class PlaygroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground)

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        applyTextSizeToAllTextViews(rootView, this)
        if (isHighContrastEnabled(this)) {
            applyHighContrastToAllViews(rootView, this)
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<MaterialCardView>(R.id.cardLiveCamera).setOnClickListener {
            startActivity(Intent(this, LiveSignRecognitionActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardRecording).setOnClickListener {
            startActivity(Intent(this, RecordVideoActivity::class.java))
        }
    }
}
