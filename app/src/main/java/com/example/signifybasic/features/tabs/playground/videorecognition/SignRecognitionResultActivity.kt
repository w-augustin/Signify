package com.example.signifybasic.features.tabs.playground.videorecognition

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.HomePage

class SignRecognitionResultActivity : AppCompatActivity() {
    private lateinit var tvRecognizedSign: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnRecordAnother: Button
    private lateinit var btnGoBack: Button
    private lateinit var tvMatchResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_recognition_result)

        // Initialize views
        tvRecognizedSign = findViewById(R.id.tvRecognizedSign)
        tvScore = findViewById(R.id.tvScore)
        btnRecordAnother = findViewById(R.id.btnRecordAnother)
        btnGoBack = findViewById(R.id.btnGoBack)
        tvMatchResult = findViewById(R.id.tvMatchResult)

        // Get recognized sign and probability from intent
        val recognizedSign = intent.getStringExtra("recognizedSign")
        val signScore = intent.getStringExtra("score")
        val matchResult = intent.getStringExtra("matchResult") ?: "Match result unavailable"

        // Display the result
        tvRecognizedSign.text = "Recognized Sign: $recognizedSign"
        tvScore.text = "Confidence: $signScore"
        tvMatchResult.text = matchResult

        // Handle "Record Another Video" button click
        btnRecordAnother.setOnClickListener {
            val intent = Intent(this, RecordVideoActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle "Go Back to App" button click
        btnGoBack.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
