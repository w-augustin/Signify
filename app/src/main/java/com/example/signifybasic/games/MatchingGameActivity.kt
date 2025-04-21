package com.example.signifybasic.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.google.android.material.card.MaterialCardView

class MatchingGameActivity : BaseGameActivity() {
    override fun getGameLayoutId(): Int = R.layout.activity_matching_game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signColumn = findViewById<LinearLayout>(R.id.sign_column)
        val optionColumn = findViewById<LinearLayout>(R.id.option_column)

        val actionButtonCard = findViewById<MaterialCardView>(R.id.action_button_card)
        val actionButtonText = findViewById<TextView>(R.id.action_button_text)

        val resetButton = findViewById<MaterialCardView>(R.id.reset_button_card)

        val signs = mutableListOf<ImageButton>()
        val options = mutableListOf<Button>()

        val stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val step = module.games.getOrNull(stepIndex)

        if (step == null || step.type != "matching") {
            Toast.makeText(this, "Error loading matching game", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val resultKey = step.resultKey

        val signData = step.items?.mapNotNull {
            val letter = it["letter"]
            val imageName = it["imageRes"]
            if (letter != null && imageName != null) {
                val resId = resources.getIdentifier(imageName, "drawable", packageName)
                MatchingItem(letter, resId)
            } else null
        } ?: emptyList()


        val randomizedSigns = signData.shuffled()
        signData.forEach { item ->
            val sign = ImageButton(this).apply {
                setImageResource(item.drawableRes)
                contentDescription = item.letter
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(0, 0, 0, 24)
                }

                background = ContextCompat.getDrawable(context, R.drawable.rounded_white)
                clipToOutline = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(4, 4, 4, 4)

                val typedValue = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
                foreground = ContextCompat.getDrawable(context, typedValue.resourceId)

                isClickable = true
                isFocusable = true
            }
            signColumn.addView(sign)
            signs.add(sign)
        }

        randomizedSigns.forEach { item ->
            val option = Button(this).apply {
                text = item.letter
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(0, 0, 0, 24)
                }
                setTextColor(ContextCompat.getColor(context, R.color.white))
                textSize = 24f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            optionColumn.addView(option)
            options.add(option)
        }


        // Logic
        var matchedCount = 0
        val totalMatches = signs.size
        var lastClickedSign: ImageButton? = null
        var btnSelected = false

        signs.forEach { sign ->
            sign.setOnClickListener {
                if (!btnSelected && sign.isEnabled) {
                    lastClickedSign = sign
                    sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.light_blue)
                    btnSelected = true
                }
            }
        }

        options.forEach { option ->
            option.setOnClickListener {
                if (btnSelected && lastClickedSign != null && option.isEnabled) {
                    val sign = lastClickedSign!!
                    val correct = sign.contentDescription.toString().equals(option.text.toString(), ignoreCase = true)

                    if (correct) {
                        sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                        option.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                        matchedCount++
                        sign.isEnabled = false
                        option.isEnabled = false
                    } else {
                        sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                        option.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                    }

                    if (matchedCount == totalMatches){
                        actionButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                        actionButtonText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    }

                    lastClickedSign = null
                    btnSelected = false
                }
            }
        }

        actionButtonCard.setOnClickListener {
            if (matchedCount == totalMatches) {
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

                val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
                val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size - 1
                val intent = Intent(this, com.example.signifybasic.features.activitycenter.ActivityCenter::class.java)
                intent.putExtra("IS_CORRECT", true)

                // if more activities remain in this module, simply continue
                if (!isLastStep) {
                    ModuleManager.moveToNextStep()
                    DBHelper(this).updateUserProgress(username, ModuleManager.currentModuleIndex, ModuleManager.currentStepIndex)
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
            } else {
                Toast.makeText(this, "You got $matchedCount / $totalMatches correct", Toast.LENGTH_SHORT).show()
            }
        }

        resetButton.setOnClickListener {
            if (matchedCount == totalMatches){
                return@setOnClickListener
            }
            matchedCount = 0
            btnSelected = false
            lastClickedSign = null

            signs.forEach {
                it.isEnabled = true
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

            options.forEach {
                it.isEnabled = true
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.signify_blue)
            }
        }
    }
}
