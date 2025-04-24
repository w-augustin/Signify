package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

// helper classes to establish game data
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
        // basic xml establishment
        super.onCreate(savedInstanceState)

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val step = module.games.getOrNull(stepIndex)

        if (step == null || step.type != "fill_blank") {
            Toast.makeText(this, "Error loading game step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // getting all the game data and dynamically setting up xml
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

        val actionButtonCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionButtonText = findViewById<TextView>(R.id.action_button_text)

        val buttons = listOf(
            findViewById<ImageButton>(R.id.t_sign),
            findViewById<ImageButton>(R.id.p_sign),
            findViewById<ImageButton>(R.id.r_sign),
            findViewById<ImageButton>(R.id.b_sign)
        )

        gameData.options.forEachIndexed { index, option ->
            val btn = buttons[index]
            btn.setImageResource(option.imageRes)
            btn.background = ContextCompat.getDrawable(this, R.drawable.rounded_white)
            btn.clipToOutline = true
            btn.scaleType = ImageView.ScaleType.CENTER_CROP
            btn.contentDescription = option.letter
            btn.setOnClickListener {
                selectedButton = btn
                selectedAnswer = option.letter
                btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.light_blue)
            }
        }

        // allow selection of answer
        actionButtonCard.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buttons.forEach {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

            // correct answer submitted - move forward, etc.
            if (selectedAnswer.equals(gameData.correctAnswer, ignoreCase = true)) {
                selectedButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                isCorrect = true

                actionButtonText.text = "Continue"
                actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

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
                android.util.Log.d("PROGRESS", "Saved progress: module=$modIndex, step=$stepIndex")

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
                // bad answer submitted - don't move forward
                selectedButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()

                actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red))
                actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))

                actionButtonCard.postDelayed({
                    actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                    actionButtonText.setTextColor(ContextCompat.getColor(this, R.color.signify_blue))
                }, 2000)
            }
        }    }
}
