package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.base.BaseGameActivity
import com.example.signifybasic.features.games.MatchingGameActivity
import com.example.signifybasic.features.games.MatchingItem

class fill_blank_game : BaseGameActivity() {

    private lateinit var tSign: ImageButton
    private lateinit var pSign: ImageButton
    private lateinit var rSign: ImageButton
    private lateinit var bSign: ImageButton
    private lateinit var submitButton: Button
    private var selectedButton: ImageButton? = null
    private var isCorrect = false

    override fun getGameLayoutId(): Int = R.layout.activity_fill_blank_game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tSign = findViewById(R.id.t_sign)
        pSign = findViewById(R.id.p_sign)
        rSign = findViewById(R.id.r_sign)
        bSign = findViewById(R.id.b_sign)
        submitButton = findViewById(R.id.submit_button)

        val buttons = listOf(tSign, pSign, rSign, bSign)

        fun resetButtons() {
            buttons.forEach {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            }
        }

        buttons.forEach { button ->
            button.setOnClickListener {
                resetButtons()
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
                selectedButton = button
            }
        }

        submitButton.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetButtons()

            if (selectedButton == tSign) {
                tSign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                isCorrect = true
                submitButton.text = "Continue"
                submitButton.setOnClickListener {
                    val signSet = arrayListOf(
                        MatchingItem("p", R.drawable.p_sign),
                        MatchingItem("a", R.drawable.a_sign),
                        MatchingItem("c", R.drawable.c_sign),
                        MatchingItem("b", R.drawable.b_sign)
                    )

                    val intent = Intent(this, MatchingGameActivity::class.java).apply {
                        putExtra("SIGN_DATA", signSet)
                        putExtra("NEXT_GAME_CLASS", "com.example.signifybasic.signing_game")
                        putExtra("RESULT_KEY", "MATCHING_BOOL") // or "FILL_BLANK_BOOL" if that’s what you want to receive
                    }

                    startActivity(intent)
                }

            } else {
                selectedButton!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
