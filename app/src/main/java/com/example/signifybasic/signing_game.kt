package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.games.MatchingGameActivity
import com.example.signifybasic.features.games.MatchingItem

class signing_game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signing_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val continueBtn = findViewById<Button>(R.id.continue_button)
        continueBtn.setOnClickListener {
            val signSet = arrayListOf(
                MatchingItem("u", R.drawable.u_sign),
                MatchingItem("v", R.drawable.v_sign),
                MatchingItem("x", R.drawable.x_sign),
                MatchingItem("w", R.drawable.w_sign)
            )

            val intent = Intent(this, MatchingGameActivity::class.java).apply {
                putExtra("SIGN_DATA", signSet)
                putExtra("NEXT_GAME_CLASS", "com.example.signifybasic.identify_game2")
                putExtra("RESULT_KEY", "MATCHING2_BOOL")
            }

            startActivity(intent)

        }

    }
}