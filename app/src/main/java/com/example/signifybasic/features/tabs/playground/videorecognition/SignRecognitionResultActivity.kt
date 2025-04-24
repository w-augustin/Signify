package com.example.signifybasic.features.tabs.playground.videorecognition

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.HomePage
import com.example.signifybasic.features.tabs.dictionary.DictionaryFragment

class SignRecognitionResultActivity : AppCompatActivity() {
    private lateinit var btnRecordAnother: Button
    private lateinit var btnGoBack: Button
    private lateinit var predictionsTextView: TextView
    private lateinit var btnGoToDictionary: Button
    private lateinit var fragmentContainer: FragmentContainerView
    private lateinit var dictionaryTextView: TextView
    private lateinit var inputtedSignTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_recognition_result)

        btnRecordAnother = findViewById(R.id.btnRecordAnother)
        btnGoBack = findViewById(R.id.btnGoBack)
        btnGoToDictionary = findViewById(R.id.btnGoToDictionary)
        predictionsTextView = findViewById(R.id.predictionsTextView)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        dictionaryTextView = findViewById(R.id.dictionaryTextView)
        inputtedSignTextView = findViewById(R.id.inputtedSignTextView)

        // Retrieve the predictions list passed from RecordVideoActivity
        val predictionsList = intent.getStringArrayListExtra("predictionsList")
        val inputtedSign  = intent.getStringExtra("inputtedSign")

        inputtedSignTextView.text = "You signed: $inputtedSign"


        // Display the predictions in the TextView
        if (predictionsList != null && predictionsList.isNotEmpty()) {
            predictionsTextView.text = predictionsList.joinToString("\n")
        } else {
            predictionsTextView.text = "No predictions found, ensure video has 30+ frames."
        }

        if (predictionsList != null && predictionsList.size > 1) {
            btnGoToDictionary.visibility = View.VISIBLE
            dictionaryTextView.visibility = View.VISIBLE

        }

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

        // Handle "Go to Dictionary" button click
        btnGoToDictionary.setOnClickListener {
            // Replace the content of the fragment container with the DictionaryFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DictionaryFragment())
                .commit()

            // Hide the predictions TextView and make the fragment container visible
            fragmentContainer.visibility = View.VISIBLE
        }
    }
}
