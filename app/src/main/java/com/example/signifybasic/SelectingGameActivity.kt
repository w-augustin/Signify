package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.base.BaseGameActivity
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
        val step = GameSequenceManager.sequence.getOrNull(stepIndex)

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


        val question = findViewById<TextView>(R.id.question)
        val imageView = findViewById<ImageView>(R.id.p_sign)
        val optionButtons = listOf(
            findViewById<Button>(R.id.option1),
            findViewById<Button>(R.id.option2),
            findViewById<Button>(R.id.option3),
            findViewById<Button>(R.id.option4)
        )
        val submitButton = findViewById<Button>(R.id.submit_button)

        question.text = gameData.prompt
        imageView.setImageResource(gameData.imageRes)

        gameData.options.forEachIndexed { index, label ->
            optionButtons[index].text = label
        }

        optionButtons.forEach { btn ->
            btn.setOnClickListener {
                resetButtonStyles(optionButtons)
                selectedButton = btn
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
            }
        }

        submitButton.setOnClickListener {
            if (!answeredCorrectly) {
                if (selectedButton == null) {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedText = selectedButton!!.text.toString()
                val correct = selectedText.equals(gameData.correctAnswer, ignoreCase = true)

                resetButtonStyles(optionButtons)

                if (correct) {
                    selectedButton!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                    optionButtons.forEach { it.isEnabled = false }
                    submitButton.text = "Continue"
                    answeredCorrectly = true
                } else {
                    selectedButton!!.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                    Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show()
                    selectedButton = null
                }
            } else {
                val intent = Intent(this, com.example.signifybasic.features.activitycenter.ActivityCenter::class.java)
                intent.putExtra("IS_CORRECT", true)
                intent.putExtra(gameData.resultKey, true)
                intent.putExtra("CONTINUE_SEQUENCE", true)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun resetButtonStyles(buttons: List<Button>) {
        buttons.forEach {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
            it.isEnabled = true
        }
        val returnbtn = findViewById<Button>(R.id.return_button)
        returnbtn.setOnClickListener {
            startActivity(Intent(this, ActivityCenter::class.java))
        }
    }
}
