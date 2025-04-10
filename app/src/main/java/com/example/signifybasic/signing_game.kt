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
            val intent = Intent(this, com.example.signifybasic.features.activitycenter.ActivityCenter::class.java)
            intent.putExtra("IS_CORRECT", true)
            intent.putExtra("SIGNING_BOOL", true)
            intent.putExtra("CONTINUE_SEQUENCE", true)
            startActivity(intent)
            finish()
        }


    }
}