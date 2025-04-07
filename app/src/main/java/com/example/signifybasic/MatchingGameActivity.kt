package com.example.signifybasic.features.games

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.features.base.BaseGameActivity

class MatchingGameActivity : BaseGameActivity() {
    override fun getGameLayoutId(): Int = R.layout.activity_matching_game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signContainer = findViewById<LinearLayout>(R.id.sign_container)
        val optionContainer = findViewById<LinearLayout>(R.id.option_container)
        val continueButton = findViewById<Button>(R.id.continue_button)
        val resetButton = findViewById<Button>(R.id.reset_btn)

        val signs = mutableListOf<ImageButton>()
        val options = mutableListOf<Button>()

        val signData = intent.getSerializableExtra("SIGN_DATA") as? List<MatchingItem> ?: emptyList()
        val nextGameClass = Class.forName(intent.getStringExtra("NEXT_GAME_CLASS")!!)
        val resultKey = intent.getStringExtra("RESULT_KEY") ?: "MATCHING_BOOL"

        signData.forEach { item ->
            val sign = ImageButton(this).apply {
                setImageResource(item.drawableRes)
                contentDescription = item.letter
                layoutParams = LinearLayout.LayoutParams(150, 140).apply {
                    setMargins(0, 0, 0, 24)
                }
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                scaleType = ImageView.ScaleType.FIT_XY
            }

            val option = Button(this).apply {
                text = item.letter
                layoutParams = LinearLayout.LayoutParams(150, 130).apply {
                    setMargins(0, 0, 0, 24)
                }
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.primary_blue)
                setTextColor(ContextCompat.getColor(context, R.color.white))
                textSize = 24f
            }

            signContainer.addView(sign)
            optionContainer.addView(option)

            signs.add(sign)
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
                    sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
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

                    lastClickedSign = null
                    btnSelected = false
                }
            }
        }

        continueButton.setOnClickListener {
            if (matchedCount == totalMatches) {
                val intent = Intent(this, nextGameClass)
                intent.putExtra(resultKey, true)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "You got $matchedCount / $totalMatches correct", Toast.LENGTH_SHORT).show()
            }
        }

        resetButton.setOnClickListener {
            matchedCount = 0
            btnSelected = false
            lastClickedSign = null

            signs.forEach {
                it.isEnabled = true
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }

            options.forEach {
                it.isEnabled = true
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
            }
        }
    }
}
