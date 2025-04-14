package com.example.signifybasic.features.activitycenter

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.games.FillBlankGameActivity
import com.example.signifybasic.features.games.FillBlankGameData
import com.example.signifybasic.features.games.FillBlankOption
import com.example.signifybasic.R
import com.example.signifybasic.features.games.SelectingGameActivity
import com.example.signifybasic.features.games.SelectingGameData
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.games.IdentifyGameActivity
import com.example.signifybasic.features.games.IdentifyGameData
import com.example.signifybasic.features.games.IdentifyOption
import com.example.signifybasic.features.games.MatchingGameActivity
import com.example.signifybasic.features.games.MatchingItem
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.example.signifybasic.getStarted
import com.google.android.material.appbar.MaterialToolbar
import com.example.signifybasic.features.games.GameRouter
import com.example.signifybasic.features.games.GameSequenceManager
import com.example.signifybasic.features.games.GameStep
import com.example.signifybasic.features.games.ModuleManager


class ActivityCenter : AppCompatActivity() {
    private var gameSequenceInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_center)

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        applyTextSizeToAllTextViews(rootView, this)
        if (isHighContrastEnabled(this)) {
            applyHighContrastToAllViews(rootView, this)
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val getStartedButton = findViewById<Button>(R.id.button2)
        getStartedButton.setOnClickListener {
            ModuleManager.loadModules(this)
            ModuleManager.resetModule(0)

            val firstStep = ModuleManager.getCurrentStep()
            if (firstStep != null) {
                GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
            } else {
                Toast.makeText(this, "Module is empty", Toast.LENGTH_SHORT).show()
            }
        }

        if (!gameSequenceInitialized) {
            GameSequenceManager.load(this)
            gameSequenceInitialized = true
        }

        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
        if (continueFromSequence) {
            ModuleManager.moveToNextStep()
            val nextStep = ModuleManager.getCurrentStep()

            if (nextStep != null) {
                GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
            } else if (!ModuleManager.isAllModulesComplete()) {
                Toast.makeText(this, "Module complete! Moving to next module.", Toast.LENGTH_SHORT).show()
                val step = ModuleManager.getCurrentStep()
                if (step != null) {
                    GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
                }
            } else {
                Toast.makeText(this, "All modules complete!", Toast.LENGTH_LONG).show()
            }
        }

    }
}
