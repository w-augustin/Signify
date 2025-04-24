package com.example.signifybasic.features.tabs.dictionary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.tabs.playground.liverecognition.SignAnalyzer
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DictionaryGame : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var predictionLabel: TextView
    private lateinit var wordInput: EditText
    private lateinit var goButton: Button
    private lateinit var selectButtons: List<Button>
    private lateinit var letterBoxContainer: LinearLayout

    private var selectedIndex = 0
    private var letterStates = mutableListOf<String>()
    private var topPredictions = listOf<String>()
    private var wordToSpell = ""

    private var lastUpdateTime = 0L
    private val updateInterval = 2500L

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // set up basic xml
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_spell_game)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        previewView = findViewById(R.id.previewView)
        predictionLabel = findViewById(R.id.predictions_guide)
        wordInput = findViewById(R.id.wordInput)
        goButton = findViewById(R.id.goButton)
        letterBoxContainer = findViewById(R.id.letterBoxContainer)
        selectButtons = listOf(
            findViewById(R.id.select_button_1),
            findViewById(R.id.select_button_2),
            findViewById(R.id.select_button_3)
        )

        // when user clicks go, take the word and start the game
        goButton.setOnClickListener {
            // input validation
            val input = wordInput.text.toString().uppercase()
            if (input.isBlank() || input.length > 4) {
                Toast.makeText(this, "Enter a word with 1–4 letters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // make changes to UI to reflect beginning of game
            wordToSpell = input
            selectedIndex = 0
            letterStates = MutableList(wordToSpell.length) { "" }
            renderLetterBoxes()

            findViewById<LinearLayout>(R.id.wordEntryRow).visibility = LinearLayout.GONE

            findViewById<LinearLayout>(R.id.wordEntryRow).visibility = LinearLayout.GONE
            findViewById<TextView>(R.id.wordDisplay).apply {
                text = wordToSpell
                visibility = View.VISIBLE
            }

            val constraintSet = ConstraintSet()
            val layout = findViewById<ConstraintLayout>(R.id.freeSpellLayout)
            constraintSet.clone(layout)

            constraintSet.clear(R.id.letterBoxContainer, ConstraintSet.TOP)
            constraintSet.connect(
                R.id.letterBoxContainer,
                ConstraintSet.TOP,
                R.id.wordDisplay,
                ConstraintSet.BOTTOM,
                16
            )

            constraintSet.applyTo(layout)

        }

        // allow user to access the top three options for their sign
        selectButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (wordToSpell.isBlank()) {
                    Toast.makeText(this, "Enter a word first!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val prediction = topPredictions.getOrNull(index)
                if (!prediction.isNullOrBlank()) {
                    letterStates[selectedIndex] = prediction
                    updateLetterBoxes()
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }

        val submitButton = findViewById<Button>(R.id.submitButton)

        // when user attempts to submit a word
        submitButton.setOnClickListener {
            val userWord = letterStates.joinToString("")
            if (userWord.equals(wordToSpell, ignoreCase = true)) {
                // update user's known words
                val dbHelper = DBHelper(this)
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", null)
                val userId = username?.let { it1 -> dbHelper.getUserIdByUsername(it1) }
                if (userId != null) {
                    dbHelper.addKnownWord(userId, wordToSpell)
                }
                predictionLabel.text = "You spelled '$wordToSpell' correctly!"

                // reset xml to allow for new game to begin
                findViewById<TextView>(R.id.wordDisplay).visibility = View.GONE
                findViewById<LinearLayout>(R.id.wordEntryRow).visibility = View.VISIBLE
                wordInput.setText("")
                letterBoxContainer.removeAllViews()
                val constraintSet = ConstraintSet()
                val layout = findViewById<ConstraintLayout>(R.id.freeSpellLayout)
                constraintSet.clone(layout)
                constraintSet.clear(R.id.letterBoxContainer, ConstraintSet.TOP)
                constraintSet.connect(
                    R.id.letterBoxContainer,
                    ConstraintSet.TOP,
                    R.id.wordEntryRow,
                    ConstraintSet.BOTTOM,
                    16
                )
                constraintSet.applyTo(layout)

                findViewById<LinearLayout>(R.id.wordEntryRow).visibility = LinearLayout.VISIBLE

                wordInput.setText("")
                letterBoxContainer.removeAllViews()
            } else {
                predictionLabel.text = "'$userWord' is not correct. Try again!"
            }
        }

    }

    // function to assist in camera use
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    // dynamically render the amount of boxes necessary for the chosen word
    private fun renderLetterBoxes() {
        letterBoxContainer.removeAllViews()
        wordToSpell.forEachIndexed { index, _ ->
            val box = MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(180, 180).apply {
                    setMargins(12, 12, 12, 12)
                }
                text = "_"
                textSize = 24f
                setTextColor(ContextCompat.getColor(this@DictionaryGame, R.color.signify_blue))
                setPadding(0, 0, 0, 0)
                alpha = if (index == selectedIndex) 1f else 0.5f
                setOnClickListener {
                    selectedIndex = index
                    updateLetterBoxes()
                }
            }
            letterBoxContainer.addView(box)
        }
        updateLetterBoxes()
    }

    // update the letter boxes as letters are signed and selected
    private fun updateLetterBoxes() {
        for (i in 0 until letterBoxContainer.childCount) {
            val btn = letterBoxContainer.getChildAt(i) as MaterialButton
            val letter = letterStates.getOrNull(i)
            btn.text = letter?.ifBlank { "_" } ?: "_"
            btn.alpha = if (i == selectedIndex) 1f else 0.5f
        }

        // Just update instructions, not correctness
        if (letterStates.none { it.isBlank() }) {
            predictionLabel.text = "All letters selected. Press submit to check."
        } else {
            predictionLabel.text = "Choose a sign for letter ${selectedIndex + 1}"
        }
    }

    // function for launching camera
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, SignAnalyzer(this) { predictionText ->
                        runOnUiThread {
                            val now = System.currentTimeMillis()
                            if (now - lastUpdateTime > updateInterval) {
                                lastUpdateTime = now

                                val topLines = predictionText.lines().filter { it.contains(":") }
                                topPredictions = topLines.map { it.take(1) }

                                selectButtons.getOrNull(0)?.text = topLines.getOrNull(0) ?: "--"
                                selectButtons.getOrNull(1)?.text = topLines.getOrNull(1) ?: "--"
                                selectButtons.getOrNull(2)?.text = topLines.getOrNull(2) ?: "--"
                            }

                        }
                    })
                }

            val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                Toast.makeText(this, "No available camera found", Toast.LENGTH_LONG).show()
                return@addListener
            }
            cameraProvider.availableCameraInfos.forEachIndexed { index, info ->
                Log.d("CameraInfo", "Camera #$index available.")
            }


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@DictionaryGame as LifecycleOwner, cameraSelector, preview, analyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
