package com.example.signifybasic.features.base

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseGameActivity : AppCompatActivity() {
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_game)

        val container = findViewById<FrameLayout>(R.id.gameContentContainer)
        layoutInflater.inflate(getGameLayoutId(), container, true)

        progressBar = findViewById(R.id.moduleProgressBar)
        progressBar.max = 100
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    abstract fun getGameLayoutId(): Int
}
