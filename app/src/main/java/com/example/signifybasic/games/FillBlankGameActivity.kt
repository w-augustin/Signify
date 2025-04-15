package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.games.BaseGameActivity
import com.example.signifybasic.games.GameSequenceManager
import com.example.signifybasic.games.ModuleManager
import java.io.Serializable

data class FillBlankGameData(
    val prompt: String,
    val question: String,
    val options: List<FillBlankOption>,
    val correctAnswer: String,
    val nextGameClass: String?,
    val resultKey: String
) : Serializable

data class FillBlankOption(
    val letter: String,
    val imageRes: Int
) : Serializable

class FillBlankGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_fill_blank_game

    private var selectedButton: ImageButton? = null
    private var selectedAnswer: String? = null
    private var isCorrect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val step = GameSequenceManager.sequence.getOrNull(stepIndex)

        if (step == null || step.type != "fill_blank") {
            Toast.makeText(this, "Error loading game step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val options = step.options?.mapNotNull {
            when (it) {
                is Map<*, *> -> {
                    val letter = it["letter"] as? String
                    val imageName = it["imageRes"] as? String
                    if (letter != null && imageName != null) {
                        val resId = resources.getIdentifier(imageName, "drawable", packageName)
                        FillBlankOption(letter, resId)
                    } else null
                }
                else -> null
            }
        } ?: emptyList()

        val gameData = FillBlankGameData(
            prompt = step.prompt ?: "Fill in the blank",
            question = step.question ?: "Choose the correct sign",
            options = options,
            correctAnswer = step.correctAnswer ?: "",
            nextGameClass = null,
            resultKey = step.resultKey
        )



        findViewById<TextView>(R.id.prompt).text = gameData.prompt
        findViewById<TextView>(R.id.question).text = gameData.question

        val submitButton = findViewById<Button>(R.id.submit_button)
        val buttons = listOf(
            findViewById<ImageButton>(R.id.t_sign),
            findViewById<ImageButton>(R.id.p_sign),
            findViewById<ImageButton>(R.id.r_sign),
            findViewById<ImageButton>(R.id.b_sign)
        )

        gameData.options.forEachIndexed { index, option ->
            val btn = buttons[index]
            btn.setImageResource(option.imageRes)
            btn.contentDescription = option.letter
            btn.setOnClickListener {
                buttons.forEach {
                    it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
                }
                selectedButton = btn
                selectedAnswer = option.letter
                btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.light_blue)
            }
        }

        submitButton.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buttons.forEach {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

            if (selectedAnswer.equals(gameData.correctAnswer, ignoreCase = true)) {
                selectedButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                isCorrect = true
                submitButton.text = "Continue"

                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

                com.example.signifybasic.games.ModuleManager.moveToNextStep()
                val modIndex = com.example.signifybasic.games.ModuleManager.currentModuleIndex
                val stepIndex = com.example.signifybasic.games.ModuleManager.currentStepIndex

                com.example.signifybasic.database.DBHelper(this).updateUserProgress(username, modIndex, stepIndex)
                android.util.Log.d("PROGRESS", "Saved progress: module=$modIndex, step=$stepIndex")

                submitButton.setOnClickListener {
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
