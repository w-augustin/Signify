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
import com.example.signifybasic.games.ModuleManager
import com.google.android.material.card.MaterialCardView
import java.io.Serializable

// helper class for importing data
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
        // set basic xml
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val step = module.games.getOrNull(stepIndex)

        if (step == null || step.type != "identify") {
            Toast.makeText(this, "Error loading game step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // dynamically set up xml according to data
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
            //setting the game up
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

        val optionButtons = listOfNotNull(
            findViewById<ImageButton?>(R.id.option_image_1),
            findViewById<ImageButton?>(R.id.option_image_2),
            findViewById<ImageButton?>(R.id.option_image_3),
            findViewById<ImageButton?>(R.id.option_image_4)
        )

        // load up to 4 options
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

        val actionButtonCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionButtonText = findViewById<TextView>(R.id.action_button_text)

        // on submit
        // user did not select an answer but try to move forward
        actionButtonCard.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedAnswer = selectedButton?.contentDescription?.toString()
            val isCorrect = selectedAnswer.equals(correctAnswer, ignoreCase = true)

            resetImageButtonStyles(optionButtons)

            // if user is correct, move on etc.
            if (isCorrect) {
                actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                actionButtonText.text = "Continue"
                optionButtons.forEach { it.isEnabled = false }

                actionButtonText.text = "Continue"
                identifyCorrect = true

                // update progress
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
                ModuleManager.moveToNextStep()
                val modIndex = ModuleManager.currentModuleIndex
                val stepIndex = ModuleManager.currentStepIndex

                val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
                val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size - 1

                if (!isLastStep){
                    DBHelper(this).updateUserProgress(username, ModuleManager.currentModuleIndex, ModuleManager.currentStepIndex)
                }
                else {
                    DBHelper(this).updateUserProgress(username, modIndex, stepIndex)
                }
                Log.d("PROGRESS", "Correct! Saved progress: module=$modIndex, step=$stepIndex")

                // update listener to continue
                actionButtonCard.setOnClickListener {
                    val intent = Intent(this, ActivityCenter::class.java)
                    intent.putExtra("IS_CORRECT", true)

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

            } else {
                // if user is wrong, don't progress
                actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red))
                actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

                Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()

                actionButtonCard.postDelayed({
                    actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                    actionButtonText.setTextColor(ContextCompat.getColor(this, R.color.signify_blue))
                }, 2500)

                selectedButton = null
            }
        }
    }

    // reset the image buttons to default state
    private fun resetImageButtonStyles(buttons: List<ImageButton>) {
        buttons.forEach {
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            it.isEnabled = true
        }
    }

}
