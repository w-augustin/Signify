package com.example.signifybasic.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.playground.liverecognition.SignAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.games.ModuleManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SpellWordGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_spell_word_game

    private lateinit var promptTextView: TextView
    private lateinit var predictionLabel: TextView
    private lateinit var selectButton1: Button
    private lateinit var selectButton2: Button
    private lateinit var selectButton3: Button

    private lateinit var letterBoxes: List<MaterialButton>

    private lateinit var word: String
    private var selectedIndex = 0
    private var letterStates: MutableList<String> = mutableListOf()
    private var topPredictions = listOf<String>()
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lastUpdateTime = 0L
    private val updateInterval = 2500L // ms

    private var incorrectAttempts = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val step = module.games.getOrNull(stepIndex)

        word = step?.correctAnswer ?: "CAT"
        letterStates = MutableList(word.length) { "" }

        promptTextView = findViewById(R.id.prompt)
        predictionLabel = findViewById(R.id.predictions_guide)
        selectButton1 = findViewById(R.id.select_button_1)
        selectButton2 = findViewById(R.id.select_button_2)
        selectButton3 = findViewById(R.id.select_button_3)
        val actionCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionText = findViewById<TextView>(R.id.action_button_text)

        actionCard.isEnabled = false
        actionCard.alpha = 0.5f

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

                    val allFilled = letterStates.none { it.isBlank() }
                    actionCard.isEnabled = allFilled
                    actionCard.alpha = if (allFilled) 1.0f else 0.5f
                }
            }
        }

        letterBoxes.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedIndex = index
                updateLetterBoxes()
            }
        }

        actionText.setOnClickListener {
            if (!actionCard.isEnabled) return@setOnClickListener

            val spelled = letterStates.joinToString("")
            if (spelled.equals(word, ignoreCase = true)) {
                handleSuccess()
            } else {
                incorrectAttempts++


                if (incorrectAttempts >= maxAttempts) {
                    Toast.makeText(this, "Max attempts reached. You may continue.", Toast.LENGTH_SHORT).show()
                    handleSuccess()
                } else {
                    flashErrorOnButton()
                    Toast.makeText(this, "Incorrect spelling. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun flashErrorOnButton() {
        val actionCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionText = findViewById<TextView>(R.id.action_button_text)

        // Save current colors
        val defaultBg = ContextCompat.getColor(this, android.R.color.white)
        val defaultText = ContextCompat.getColor(this, R.color.primary_blue)

        // Set error colors
        actionCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        actionText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        // Revert after 2 seconds (toast duration)
        actionCard.postDelayed({
            if (actionText.text.toString() != "Continue") {
                actionCard.setCardBackgroundColor(defaultBg)
                actionText.setTextColor(defaultText)
            }
        }, 2500)
    }

    private fun handleSuccess() {
        val spelled = letterStates.joinToString("")
        predictionLabel.text = "You spelled $spelled correctly!"

        val actionCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionText = findViewById<TextView>(R.id.action_button_text)
        actionText.text = "Continue"
        actionCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
        actionText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        cameraExecutor.shutdown()
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(this).get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e("SpellWordGame", "Camera release error", e)
        }

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null) // Retrieve username
        ModuleManager.moveToNextStep()
        DBHelper(this).updateUserProgress(username ?: "admin", ModuleManager.currentModuleIndex, ModuleManager.currentStepIndex)

        actionText.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
            val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size

            if (!isLastStep) {
                // Only set CONTINUE_SEQUENCE if there's more to do
                intent.putExtra("CONTINUE_SEQUENCE", true)
            }

            intent.putExtra("IS_CORRECT", true)
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
