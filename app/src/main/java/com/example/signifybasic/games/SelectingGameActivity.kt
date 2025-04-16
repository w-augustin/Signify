package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.games.BaseGameActivity
import com.example.signifybasic.games.ModuleManager
import com.google.android.material.card.MaterialCardView
import java.io.Serializable

data class SelectingGameData(
    val imageRes: Int,
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val nextGameClass: String?,
    val resultKey: String
) : Serializable

class SelectingGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_selecting_game

    private var selectedButton: Button? = null
    private var answeredCorrectly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val step = module.games.getOrNull(stepIndex)


        if (step == null || step.type != "selecting") {
            Toast.makeText(this, "Error loading game data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageResId = resources.getIdentifier(step.imageRes, "drawable", packageName)

        val options = step.options?.mapNotNull {
            when (it) {
                is Map<*, *> -> it["option"] as? String
                is String -> it // fallback in case your JSON isn't updated
                else -> null
            }
        } ?: emptyList()

        val gameData = SelectingGameData(
            imageRes = imageResId,
            prompt = step.prompt ?: "What is this?",
            options = options,
            correctAnswer = step.correctAnswer ?: "",
            nextGameClass = null, // We'll override this logic at the bottom
            resultKey = step.resultKey
        )


        val question = findViewById<TextView>(R.id.prompt)
        val imageView = findViewById<ImageView>(R.id.p_sign)
        val optionButtons = listOf(
            findViewById<Button>(R.id.option1),
            findViewById<Button>(R.id.option2),
            findViewById<Button>(R.id.option3),
            findViewById<Button>(R.id.option4)
        )
        val actionButtonCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionButtonText = findViewById<TextView>(R.id.action_button_text)

        question.text = gameData.prompt
        imageView.setImageResource(gameData.imageRes)

        gameData.options.forEachIndexed { index, label ->
            optionButtons[index].text = label
        }

        optionButtons.forEach { btn ->
            btn.setOnClickListener {
                selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.signify_blue))
                selectedButton = btn
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
            }
        }

        actionButtonCard.setOnClickListener {
            if (!answeredCorrectly) {
                if (selectedButton == null) {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedText = selectedButton!!.text.toString()
                val correct = selectedText.equals(gameData.correctAnswer, ignoreCase = true)

                resetButtonStyles(optionButtons)

                if (correct) {
                    selectedButton!!.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                    optionButtons.forEach { it.isEnabled = false }

                    actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                    actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    actionButtonText.text = "Continue"
                    answeredCorrectly = true

                    // Progress update
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
                    val moduleIndex = ModuleManager.currentModuleIndex
                    val nextStep = ++ModuleManager.currentStepIndex

                    val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
                    val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size - 1

                    if (!isLastStep){
                        DBHelper(this).updateUserProgress(username, ModuleManager.currentModuleIndex, ModuleManager.currentStepIndex)
                    }
                    else {
                        DBHelper(this).updateUserProgress(username, moduleIndex, stepIndex)
                    }
                    Log.d("PROGRESS", "User '$username' now at module=$moduleIndex, step=$nextStep")

                } else {
                    actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red))
                    actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

                    Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()

                    selectedButton = null

                    // Reset color after delay
                    actionButtonCard.postDelayed({
                        actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                        actionButtonText.setTextColor(ContextCompat.getColor(this, R.color.signify_blue))
                    }, 2500)
                }

            } else {
                val intent = Intent(this, ActivityCenter::class.java)
                intent.putExtra("IS_CORRECT", true)

                val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
                val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size - 1

                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

                // if more activities remain in this module, simply continue
                if (!isLastStep) {
                    ModuleManager.moveToNextStep()
                    intent.putExtra("CONTINUE_SEQUENCE", true)
                } else {
                    // no more activities - send back to activity center
                    val nextModuleIndex = ModuleManager.currentModuleIndex + 1
                    if (nextModuleIndex < ModuleManager.getModules().size) { // we are moving to next module
                        DBHelper(this).updateUserProgress(username, nextModuleIndex, 0)
                        intent.putExtra("FORCE_OVERRIDE", true)
                    } else {
                        DBHelper(this).updateUserProgress(username, ModuleManager.currentModuleIndex, ModuleManager.currentStepIndex)
                    }
                }

                startActivity(intent)
                finish()
            }
        }

    }

    private fun resetButtonStyles(buttons: List<Button>) {
        buttons.forEach {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.signify_blue))
            it.isEnabled = true
        }
    }
}
