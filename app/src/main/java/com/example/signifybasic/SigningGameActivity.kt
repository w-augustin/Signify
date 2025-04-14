package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.signifybasic.R
import com.example.signifybasic.features.base.BaseGameActivity
import com.example.signifybasic.signrecognition.RecordVideoActivity

class SigningGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_signing_game

    private lateinit var promptTextView: TextView
    private lateinit var recordButton: Button

    private lateinit var expectedSign: String
    private lateinit var resultKey: String

    private var stepIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val step = GameSequenceManager.sequence.getOrNull(stepIndex)

        if (step == null || step.type != "signing") {
            Toast.makeText(this, "Error loading signing step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        expectedSign = step.expectedSign ?: ""
        resultKey = step.resultKey ?: "SIGNING_BOOL"

        promptTextView = findViewById(R.id.prompt_text)
        recordButton = findViewById(R.id.record_video_button)

        promptTextView.text = step.prompt ?: "Sign the correct letter"

        recordButton.setOnClickListener {
            val intent = Intent(this, RecordVideoActivity::class.java)
            intent.putExtra("expectedSign", expectedSign)
            intent.putExtra("fromGame", true) // To route correctly in result screen
            intent.putExtra("resultKey", resultKey)
            intent.putExtra("STEP_INDEX", stepIndex)
            startActivity(intent)
        }
    }
}
