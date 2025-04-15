package com.example.signifybasic.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
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
            navigateToPreviousStep()
        }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_next) {
                navigateToNextStep()
                true
            } else false
        }

        val returnButton = findViewById<Button>(R.id.returnToCenterButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        updateToolbarNavigation(toolbar)
    }

    private fun updateToolbarNavigation(toolbar: MaterialToolbar) {
        val stepIndex = ModuleManager.currentStepIndex
        val moduleIndex = ModuleManager.currentModuleIndex
        val totalSteps = ModuleManager.getModules()[moduleIndex].games.size

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val (savedModule, savedStep) = DBHelper(this).getUserProgress(username)

        val canGoNext = stepIndex < totalSteps - 1 && (
                moduleIndex < savedModule || (moduleIndex == savedModule && stepIndex < savedStep)
                )

        toolbar.navigationIcon = if (stepIndex > 0) ContextCompat.getDrawable(this, R.drawable.ic_arrow_back) else null

        toolbar.menu.findItem(R.id.action_next)?.isVisible = canGoNext
    }

    private fun navigateToPreviousStep() {
        val index = ModuleManager.currentStepIndex
        if (index > 0) {
            ModuleManager.currentStepIndex = index - 1
            GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
            finish()
        }
    }

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
            GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
            finish()
        } else {
            Toast.makeText(this, "You haven't unlocked the next activity yet.", Toast.LENGTH_SHORT).show()
        }
    }


    abstract fun getGameLayoutId(): Int
}