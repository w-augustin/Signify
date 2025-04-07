package com.example.signifybasic
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.*
//import androidx.core.content.ContextCompat
//import com.example.signifybasic.features.activitycenter.ActivityCenter
//import com.example.signifybasic.features.base.BaseGameActivity
//
//class matching_game : BaseGameActivity() {
////    override fun getGameLayoutId(): Int = R.layout.activity_matching_game
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////
////        val signs = listOf(
////            findViewById<ImageButton>(R.id.p_sign),
////            findViewById(R.id.a_sign),
////            findViewById(R.id.c_sign),
////            findViewById(R.id.b_sign)
////        )
////
////        val options = listOf(
////            findViewById<Button>(R.id.p_option),
////            findViewById(R.id.a_option),
////            findViewById(R.id.c_option),
////            findViewById(R.id.b_option)
////        )
////
////        val continueButton = findViewById<Button>(R.id.continue_button)
////        val resetButton = findViewById<Button>(R.id.reset_btn)
////
////        var btnSelected = false
////        var lastClickedSign: ImageButton? = null
////        var matchedCount = 0
////        val totalMatches = signs.size
////        var matching_bool = false
////
////        signs.forEach { sign ->
////            sign.setOnClickListener {
////                if (!btnSelected && sign.isEnabled) {
////                    lastClickedSign = sign
////                    sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
////                    btnSelected = true
////                }
////            }
////        }
////
////        options.forEach { option ->
////            option.setOnClickListener {
////                if (btnSelected && lastClickedSign != null && option.isEnabled) {
////                    val sign = lastClickedSign!!
////                    val correct = sign.contentDescription.toString().equals(option.text.toString(), ignoreCase = true)
////
////                    if (correct) {
////                        sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
////                        option.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
////                        matchedCount++
////                        sign.isEnabled = false
////                        option.isEnabled = false
////                    } else {
////                        sign.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
////                        option.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
////                    }
////
////                    lastClickedSign = null
////                    btnSelected = false
////                }
////            }
////        }
////
////        continueButton.setOnClickListener {
////            if (matchedCount == totalMatches) {
////                Toast.makeText(this, "You matched them all!", Toast.LENGTH_SHORT).show()
////                matching_bool = true
////                val intent = Intent(this, signing_game::class.java)
////                intent.putExtra("MATCHING_BOOL", true)
////                startActivity(intent)
////                finish()
////            } else {
////                Toast.makeText(this, "You got $matchedCount/$totalMatches correct", Toast.LENGTH_SHORT).show()
////            }
////        }
////
////        resetButton.setOnClickListener {
////            matchedCount = 0
////            btnSelected = false
////            lastClickedSign = null
////
////            signs.forEach {
////                it.isEnabled = true
////                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
////            }
////            options.forEach {
////                it.isEnabled = true
////                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
////            }
////        }
////    }
//}
