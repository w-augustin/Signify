package com.example.signifybasic.features.activitycenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.example.signifybasic.features.games.GameModule
import com.example.signifybasic.features.games.IdentifyGameActivity
import com.example.signifybasic.features.games.IdentifyGameData
import com.example.signifybasic.features.games.IdentifyOption
import com.example.signifybasic.features.games.MatchingGameActivity
import com.example.signifybasic.features.games.MatchingItem
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.example.signifybasic.getStarted
import com.example.signifybasic.signing_game
import com.google.android.material.appbar.MaterialToolbar
import com.example.signifybasic.features.games.GameRouter
import com.example.signifybasic.features.games.GameSequenceManager
import com.example.signifybasic.features.games.GameStep
import com.example.signifybasic.features.games.ModuleManager
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.games.ModuleManager.currentStepIndex


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

        val start = findViewById<Button>(R.id.getStarted)
        start.setOnClickListener { startActivity(Intent(this, getStarted::class.java)) }



        // wip trying to upload module activities using json
        val nextModuleBtn = findViewById<Button>(R.id.nextMod)
        nextModuleBtn.setOnClickListener {
            val intent = Intent(this, activity_center2::class.java)
            startActivity(intent)
        }

        val getStartedButton = findViewById<Button>(R.id.button2)
        getStartedButton.setOnClickListener {
            ModuleManager.loadModules(this)
            ModuleManager.resetModule(0)

            val firstStep = ModuleManager.getCurrentStep()
            if (firstStep != null) {
                GameRouter.routeToGame(this, currentStepIndex)
            } else {
                Toast.makeText(this, "Module is empty", Toast.LENGTH_SHORT).show()
            }
        }

        if (!gameSequenceInitialized) {
            GameSequenceManager.load(this)
            gameSequenceInitialized = true
        }

        //wip module2 activities
        val sharedPref = getSharedPreferences("module_prefs", MODE_PRIVATE)
        val isModule1Complete = sharedPref.getBoolean("module1_complete", false)
        // removed isEnable feature in xml need to add later
        nextModuleBtn.isEnabled = isModule1Complete



        fun getModulesList(context: Context): List<GameModule> {
            val jsonString =
                context.assets.open("modules.json").bufferedReader().use { it.readText() }
            val type = object : com.google.gson.reflect.TypeToken<List<GameModule>>() {}.type
            return com.google.gson.Gson().fromJson(jsonString, type)
        }


        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
        if (continueFromSequence) {
            val currentModule = ModuleManager.currentModuleIndex
            val currentStep = currentStepIndex
            val modules = getModulesList(this)
            val currentModuleSize = modules.getOrNull(currentModule)?.games?.size ?: 0

            val isLastStep = currentStep == currentModuleSize - 1

            if (currentModule == 0 && isLastStep) {
                // check if it on the last module
                Toast.makeText(this, "Module 1 complete!", Toast.LENGTH_SHORT).show()


                sharedPref.edit().putBoolean("module1_complete", true).apply()
                nextModuleBtn.isEnabled = true

            } else {
                // Move to the next step normally
                ModuleManager.moveToNextStep()

                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                updateProgressBar(progressBar)

                val nextStep = ModuleManager.getCurrentStep()
                if (nextStep != null) {
                    GameRouter.routeToGame(this, currentStepIndex)
                } else {
                    Toast.makeText(this, "No more steps found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // update progress bar in bd and ui
    private fun updateProgressBar(progressBar: ProgressBar) {
        val modules = ModuleManager.getModules()
        val moduleIndex = ModuleManager.currentModuleIndex
        val stepIndex = ModuleManager.currentStepIndex
        val totalSteps = modules[moduleIndex].games.size

        val progress = ((stepIndex.toFloat() / totalSteps.toFloat()) * 100).toInt()
        progressBar.progress = progress

        val dbHelper = DBHelper(this)
        val score = (moduleIndex * 100) + stepIndex
        dbHelper.changeUserProgress("admin", score)

        Log.d("ProgressTracking", "ProgressBar updated: $progress% (Step $stepIndex of $totalSteps)")
    }
    // save progress for user
    override fun onResume() {
        super.onResume()
        ModuleManager.loadModules(this)

        val dbHelper = DBHelper(this)
        // need to update the username for all user not just admin
        val score = dbHelper.getUserProgress("admin")
        Log.d("DBScoreCheck ", "Retrieved score from DB for admin: $score")

        val savedModuleIndex = score / 100
        val savedStepIndex = score % 100

        ModuleManager.resetModule(savedModuleIndex)
        currentStepIndex = savedStepIndex

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        updateProgressBar(progressBar)
    }

}
