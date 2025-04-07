package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.signifybasic.features.base.BaseGameActivity

class identify_game : BaseGameActivity() {

    private lateinit var aSign: ImageButton
    private lateinit var pSign: ImageButton
    private lateinit var cSign: ImageButton
    private lateinit var bSign: ImageButton
    private lateinit var submitBtn: Button

    private var selectedButton: ImageButton? = null
    private var identifyCorrect = false

    override fun getGameLayoutId(): Int = R.layout.activity_identify_game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init Views
        aSign = findViewById(R.id.a_sign)
        pSign = findViewById(R.id.p_sign)
        cSign = findViewById(R.id.c_sign)
        bSign = findViewById(R.id.b_sign)
        submitBtn = findViewById(R.id.submit_button)

        val buttons = listOf(aSign, pSign, cSign, bSign)

        fun resetButtons() {
            buttons.forEach { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white) }
        }

        // Button selection logic
        buttons.forEach { button ->
            button.setOnClickListener {
                resetButtons()
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
                selectedButton = button
            }
        }

        // Submit / Continue
        submitBtn.setOnClickListener {
            if (selectedButton == null) {
                Toast.makeText(this, "Please select a sign.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetButtons()

            if (selectedButton == aSign) {
                aSign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                identifyCorrect = true
                submitBtn.text = "Continue"

                submitBtn.setOnClickListener {
                    val intent = Intent(this, fill_blank_game::class.java)
                    intent.putExtra("IDENTIFY_BOOL", true)
                    startActivity(intent)
                }

            } else {
                selectedButton?.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.red)
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                identifyCorrect = false
            }
        }
    }
}
