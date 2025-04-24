package com.example.signifybasic.games

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.google.android.material.appbar.MaterialToolbar

// default, basic game actiivty
abstract class BaseGameActivity : AppCompatActivity() {
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        // simple xml
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_game)

        val container = findViewById<FrameLayout>(R.id.gameContentContainer)
        layoutInflater.inflate(getGameLayoutId(), container, true)

        // set progress bar UI
        progressBar = findViewById(R.id.moduleProgressBar)
        progressBar.max = 100

        // allow forward/backward navigation between activities
        val backStepButton = findViewById<ImageButton>(R.id.backStepButton)
        val nextStepButton = findViewById<ImageButton>(R.id.nextStepButton)
        backStepButton.setOnClickListener {
            if (backStepButton.isEnabled) navigateToPreviousStep()
        }
        nextStepButton.setOnClickListener {
            if (nextStepButton.isEnabled) navigateToNextStep()
        }

        // allow navigation back to activity center
        val returnButton = findViewById<Button>(R.id.returnToCenterButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        updateNavigationButtons()
    }

    // consistently update nav buttons according to changes
    private fun updateNavigationButtons() {
        val backStepButton = findViewById<ImageButton>(R.id.backStepButton)
        val nextStepButton = findViewById<ImageButton>(R.id.nextStepButton)

        val stepIndex = ModuleManager.currentStepIndex
        val moduleIndex = ModuleManager.currentModuleIndex
        val totalSteps = ModuleManager.getModules()[moduleIndex].games.size

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val (savedModule, savedStep) = DBHelper(this).getUserProgress(username)

        // back button logic
        if (stepIndex == 0) {
            backStepButton.visibility = View.INVISIBLE
        } else {
            backStepButton.visibility = View.VISIBLE
            backStepButton.isEnabled = true
            backStepButton.alpha = 1f
        }

        // next button logic
        val canGoNext = stepIndex < totalSteps - 1 &&
                (moduleIndex < savedModule || (moduleIndex == savedModule && stepIndex < savedStep))

        if (stepIndex == totalSteps - 1) {
            nextStepButton.visibility = View.INVISIBLE
        } else {
            nextStepButton.visibility = View.VISIBLE
            nextStepButton.isEnabled = canGoNext
            nextStepButton.alpha = if (canGoNext) 1f else 0.2f
        }
    }

    // go back an activity
    private fun navigateToPreviousStep() {
        val index = ModuleManager.currentStepIndex
        if (index > 0) {
            ModuleManager.currentStepIndex = index - 1

            // use nice animation
            val options = android.app.ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )

            GameRouter.routeToGame(this, ModuleManager.currentStepIndex, options.toBundle())
            finish()
        }
    }

    // go forward an activity
    private fun navigateToNextStep() {
        val module = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
        val currentIndex = ModuleManager.currentStepIndex

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val (savedModule, savedStep) = DBHelper(this).getUserProgress(username)

        val canAdvance = currentIndex + 1 < module.games.size &&
                (ModuleManager.currentModuleIndex < savedModule ||
                        (ModuleManager.currentModuleIndex == savedModule && currentIndex + 1 <= savedStep))

        if (canAdvance) {
            ModuleManager.currentStepIndex = currentIndex + 1

            // use nice animation
            val options = android.app.ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )

            GameRouter.routeToGame(this, ModuleManager.currentStepIndex, options.toBundle())
            finish()
        } else {
            Toast.makeText(this, "You haven't unlocked the next activity yet.", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun getGameLayoutId(): Int
}