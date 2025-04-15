package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.games.BaseGameActivity
import com.example.signifybasic.games.GameSequenceManager
import com.example.signifybasic.games.ModuleManager
import java.io.Serializable

data class IdentifyGameData(
    val questionText: String,
    val imageOptions: List<IdentifyOption>, // up to 4
    val correctAnswer: String,
    val nextGameClass: String?,
    val resultKey: String
) : Serializable

data class IdentifyOption(
    val imageRes: Int,
    val answerLabel: String
) : Serializable


class IdentifyGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_identify_game

    private var selectedButton: ImageButton? = null
    private var correctAnswer: String = ""
    private var identifyCorrect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val step = GameSequenceManager.sequence.getOrNull(stepIndex)

        if (step == null || step.type != "identify") {
            Toast.makeText(this, "Error loading game step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val options = step.options?.mapNotNull {
            when (it) {
                is Map<*, *> -> {
                    val label = it["answerLabel"] as? String
                    val imageName = it["imageRes"] as? String
                    if (label != null && imageName != null) {
                        val resId = resources.getIdentifier(imageName, "drawable", packageName)
                        IdentifyOption(imageRes = resId, answerLabel = label)
                    } else null
                }
                else -> null
            }
        } ?: emptyList()

        val gameData = IdentifyGameData(
            questionText = step.questionText ?: "Identify the sign",
            imageOptions = options,
            correctAnswer = step.correctAnswer ?: "",
            nextGameClass = null, // no longer used
            resultKey = step.resultKey
        )


        val questionView = findViewById<TextView>(R.id.question)
        questionView.text = gameData.questionText
        correctAnswer = gameData.correctAnswer

        val optionButtons = listOf(
            findViewById<ImageButton>(R.id.p_sign),
            findViewById<ImageButton>(R.id.a_sign),
            findViewById<ImageButton>(R.id.c_sign),
            findViewById<ImageButton>(R.id.b_sign)
        )

        // Load up to 4 options
        gameData.imageOptions.forEachIndexed { index, option ->
            val button = optionButtons[index]
            button.setImageResource(option.imageRes)
            button.contentDescription = option.answerLabel
            button.setOnClickListener {
                optionButtons.forEach {
                    it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
                }
                selectedButton = button
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.light_blue)
            }
        }

        val submitBtn = findViewById<Button>(R.id.submit_button)

        submitBtn.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            optionButtons.forEach {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

            val selectedAnswer = selectedButton?.contentDescription?.toString()
            if (selectedAnswer.equals(correctAnswer, ignoreCase = true)) {
                selectedButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                identifyCorrect = true
                submitBtn.text = "Continue"

                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

                ModuleManager.moveToNextStep()
                val modIndex = ModuleManager.currentModuleIndex
                val stepIndex = ModuleManager.currentStepIndex
                DBHelper(this).updateUserProgress(username, modIndex, stepIndex)

                Log.d("PROGRESS", "Correct! Saved progress: module=$modIndex, step=$stepIndex")

                // Change to continue behavior
                submitBtn.setOnClickListener {
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

            } else {
                selectedButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
