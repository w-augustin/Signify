package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.signifybasic.R
import com.example.signifybasic.features.base.BaseGameActivity
import com.example.signifybasic.signrecognition.liverecognition.SignAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SpellWordGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_spell_word_game

    private lateinit var promptTextView: TextView
    private lateinit var previewLabel: TextView
    private lateinit var predictionLabel: TextView
    private lateinit var selectButton1: Button
    private lateinit var selectButton2: Button
    private lateinit var selectButton3: Button
    private lateinit var submitButton: Button
    private lateinit var continueButton: Button

    private lateinit var letterBoxes: List<Button>

    private lateinit var word: String
    private var selectedIndex = 0
    private var letterStates: MutableList<String> = mutableListOf()
    private var topPredictions = listOf<String>()
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lastUpdateTime = 0L
    private val updateInterval = 2500L // ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val step: GameStep? = GameSequenceManager.sequence.getOrNull(intent.getIntExtra("STEP_INDEX", -1))
        word = step?.correctAnswer ?: "CAT"
        letterStates = MutableList(word.length) { "" }

        promptTextView = findViewById(R.id.prompt)
        predictionLabel = findViewById(R.id.predictions_guide)
        selectButton1 = findViewById(R.id.select_button_1)
        selectButton2 = findViewById(R.id.select_button_2)
        selectButton3 = findViewById(R.id.select_button_3)
        submitButton = findViewById(R.id.submit_button)
        continueButton = findViewById(R.id.continue_button)

        // Dynamically initialize letter boxes
        letterBoxes = listOf(
            findViewById(R.id.letter_box_1),
            findViewById(R.id.letter_box_2),
            findViewById(R.id.letter_box_3)
        )

        updatePrompt()
        updateLetterBoxes()
        startCamera()

        listOf(selectButton1, selectButton2, selectButton3).forEachIndexed { index, button ->
            button.setOnClickListener {
                val top = topPredictions.getOrNull(index)
                if (!top.isNullOrBlank()) {
                    letterStates[selectedIndex] = top
                    updateLetterBoxes()
                    submitButton.isEnabled = letterStates.none { it.isBlank() }
                }
            }
        }

        letterBoxes.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedIndex = index
                updateLetterBoxes()
            }
        }

        submitButton.setOnClickListener {
            val spelled = letterStates.joinToString("")
            if (spelled.equals(word, ignoreCase = true)) {
                predictionLabel.text = "You spelled $spelled correctly!"
                continueButton.visibility = View.VISIBLE
                submitButton.visibility = View.GONE
            } else {
                Toast.makeText(this, "Incorrect spelling. Try again!", Toast.LENGTH_SHORT).show()
            }
        }

        continueButton.setOnClickListener {
            val intent = Intent(this, com.example.signifybasic.features.activitycenter.ActivityCenter::class.java)
            intent.putExtra("IS_CORRECT", true)
            intent.putExtra("CONTINUE_SEQUENCE", true)
            startActivity(intent)
            finish()
        }
    }

    private fun updatePrompt() {
        promptTextView.text = "Spell the word: $word"
    }

    private fun updateLetterBoxes() {
        for (i in letterBoxes.indices) {
            letterBoxes[i].text = letterStates.getOrNull(i)?.ifBlank { "_" } ?: "_"
            letterBoxes[i].alpha = if (i == selectedIndex) 1.0f else 0.5f
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, SignAnalyzer(this) { topText ->
                        runOnUiThread {
                            val now = System.currentTimeMillis()
                            if (now - lastUpdateTime > updateInterval) {
                                lastUpdateTime = now
                                predictionLabel.text = topText
                                val topLines = topText.lines().filter { it.contains(":") }
                                topPredictions = topLines.map { it.take(1) }
                                selectButton1.text = topLines.getOrNull(0) ?: "--"
                                selectButton2.text = topLines.getOrNull(1) ?: "--"
                                selectButton3.text = topLines.getOrNull(2) ?: "--"
                            }
                        }
                    })
                }

            val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                Toast.makeText(this, "No available camera found", Toast.LENGTH_SHORT).show()
                return@addListener
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("SpellWordGame", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
