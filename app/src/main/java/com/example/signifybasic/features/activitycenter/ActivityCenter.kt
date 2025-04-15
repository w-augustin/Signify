package com.example.signifybasic.features.activitycenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.R
import com.example.signifybasic.games.GameModule
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.example.signifybasic.getStarted
import com.google.android.material.appbar.MaterialToolbar
import com.example.signifybasic.games.GameRouter
import com.example.signifybasic.games.GameSequenceManager
import com.example.signifybasic.games.ModuleManager
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.games.ModuleManager.currentStepIndex


class ActivityCenter : AppCompatActivity() {
    private var gameSequenceInitialized = false

    fun getMaxUnlockedModuleIndex(context: Context): Int {
        val dbHelper = DBHelper(context)
        val username = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getString("loggedInUser", "admin") ?: "admin"

        val modules = ModuleManager.getModules()
        val (modIndex, stepIndex) = dbHelper.getUserProgress(username)

        return if (
            modIndex < modules.size &&
            stepIndex >= modules[modIndex].games.size - 1
        ) {
            // Current module fully completed, unlock next one
            (modIndex + 1).coerceAtMost(modules.size - 1)
        } else {
            modIndex
        }
    }


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

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val dbHelper = DBHelper(this)

        ModuleManager.loadModules(this)

        // Get user progress as (moduleIndex, stepIndex)
        val (modIndex, stepIndex) = dbHelper.getUserProgress(username)
        ModuleManager.currentModuleIndex = modIndex
        ModuleManager.currentStepIndex = stepIndex

        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
        if (continueFromSequence) {
            Log.d("DEBUG_FLOW", "continueFromSequence triggered")
            Log.d("DEBUG_FLOW", "Step index: ${ModuleManager.currentStepIndex}")

            val module = ModuleManager.getModules().getOrNull(ModuleManager.currentModuleIndex)
            val hasFinishedModule = module == null || ModuleManager.currentStepIndex >= module.games.size
            if (module != null) {
                Log.d("DEBUG_FLOW", "${ModuleManager.currentStepIndex}, and ${module.games.size}")
            }

            if (hasFinishedModule) {
                Toast.makeText(this, "You've completed all activities in this module!", Toast.LENGTH_SHORT).show()
                return
            }

            val step = ModuleManager.getCurrentStep()
            if (step != null) {
                GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
            } else {
                Toast.makeText(this, "No more steps found.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        setupButtons(modIndex, stepIndex)
        updateProgressBar(findViewById(R.id.progressBar), modIndex, stepIndex)

        if (!gameSequenceInitialized) {
            GameSequenceManager.load(this)
            gameSequenceInitialized = true
        }

        val moduleComplete = ModuleManager.getModules()
            .getOrNull(ModuleManager.currentModuleIndex)
            ?.games
            ?.size
            ?.let { ModuleManager.currentStepIndex >= it - 1 } ?: false

        val nextModuleButton = findViewById<Button>(R.id.nextMod)
        nextModuleButton.isEnabled = moduleComplete

        nextModuleButton.setOnClickListener {
            val nextIndex = ModuleManager.currentModuleIndex + 1
            val maxAllowed = getMaxUnlockedModuleIndex(this)

            if (nextIndex <= maxAllowed && nextIndex < ModuleManager.getModules().size) {
                // Fetch the saved step index for the next module
                val (_, savedStepIndex) = DBHelper(this).getUserProgress(username)

                ModuleManager.currentModuleIndex = nextIndex
                ModuleManager.currentStepIndex = 0

                val intent = Intent(this, ActivityCenter::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Next module is locked.", Toast.LENGTH_SHORT).show()
            }
        }




        findViewById<Button>(R.id.getStarted).setOnClickListener {
            startActivity(Intent(this, getStarted::class.java))
        }

        val moduleSpinner = findViewById<Spinner>(R.id.moduleSpinner)
        val moduleTitles = ModuleManager.getModules().map { it.title }
        val moduleAdapter = ArrayAdapter(this, R.layout.spinner_item_black, moduleTitles)
        moduleAdapter.setDropDownViewResource(R.layout.spinner_item_black)
        moduleSpinner.adapter = moduleAdapter

        // Set currently selected module
        moduleSpinner.setSelection(ModuleManager.currentModuleIndex)

        moduleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val maxUnlocked = getMaxUnlockedModuleIndex(this@ActivityCenter)

                if (position <= maxUnlocked) {
                    ModuleManager.currentModuleIndex = position
                    ModuleManager.currentStepIndex = if (position == maxUnlocked) {
                        val (_, stepIndex) = DBHelper(this@ActivityCenter).getUserProgress(username)
                        stepIndex
                    } else {
                        ModuleManager.getModules()[position].games.size - 1
                    }

                    setupButtons(position, ModuleManager.currentStepIndex)
                    updateProgressBar(findViewById(R.id.progressBar), position, ModuleManager.currentStepIndex)
                } else {
                    Toast.makeText(this@ActivityCenter, "This module is locked!", Toast.LENGTH_SHORT).show()
                    moduleSpinner.setSelection(ModuleManager.currentModuleIndex)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


    }


    private fun setupButtons(modIndex: Int, fallbackStepIndex: Int = 0) {
        val module = ModuleManager.getModules()[modIndex]
        val density = resources.displayMetrics.density
        val screenWidth = resources.displayMetrics.widthPixels
        val usableWidth = screenWidth - 2 * (50 * density).toInt()
        val buttonSize = (90 * density).toInt()
        val paddingLimit = (50 * density).toInt()

        val container = findViewById<LinearLayout>(R.id.gameButtonContainer)
        container.removeAllViews()

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val dbHelper = DBHelper(this)

        // 💡 Only get the user's progress for THIS module
        val (currentModIndex, currentStepIndex) = dbHelper.getUserProgress(username)

        for (i in module.games.indices) {
            val isEnabled = when {
                modIndex < currentModIndex -> true
                modIndex == currentModIndex -> i <= currentStepIndex
                else -> false
            }

            val button = Button(this).apply {
                text = "M$modIndex" + "A${i + 1}"
                background = ContextCompat.getDrawable(context, R.drawable.rounded_ac_button)
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                isAllCaps = false
                textSize = 16f
                this.isEnabled = isEnabled

                if (!isEnabled) alpha = 0.5f
            }

            val marginStart = if (i % 2 == 0) paddingLimit else usableWidth - buttonSize
            val params = LinearLayout.LayoutParams(buttonSize, buttonSize).apply {
                setMargins(marginStart, (16 * density).toInt(), 0, 0)
            }
            button.layoutParams = params

            button.setOnClickListener {
                if (isEnabled) {
                    ModuleManager.currentModuleIndex = modIndex
                    ModuleManager.currentStepIndex = i
                    GameRouter.routeToGame(this@ActivityCenter, i)
                }
            }

            container.addView(button)
        }
    }

    private fun updateProgressBar(progressBar: ProgressBar, modIndex: Int, stepIndex: Int) {
        val totalSteps = ModuleManager.getModules()[modIndex].games.size
        val progress = ((stepIndex.toFloat() / totalSteps.toFloat()) * 100).toInt()
        progressBar.progress = progress

        Log.d("ProgressTracking", "Progress: $progress% (Step $stepIndex of $totalSteps)")
    }

    // save progress for user
    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

        val dbHelper = DBHelper(this)
        val (savedModuleIndex, savedStepIndex) = dbHelper.getUserProgress(username)
        ModuleManager.currentModuleIndex = savedModuleIndex
        ModuleManager.currentStepIndex = savedStepIndex
        Log.d("DEBUG_FLOW", "Reloaded from DB: mod=$savedModuleIndex step=$savedStepIndex")
        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
        if (!continueFromSequence) {
            ModuleManager.loadModules(this)
            Log.d("DEBUG_FLOW", "Retrieved score from DB for $username: module=$savedModuleIndex, step=$savedStepIndex")

            ModuleManager.currentModuleIndex = savedModuleIndex
            ModuleManager.currentStepIndex = savedStepIndex

            // Refresh progress bar
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            updateProgressBar(progressBar, savedModuleIndex, savedStepIndex)

            // 🔥 NEW: Refresh activity buttons based on latest progress
            setupButtons(savedModuleIndex, savedStepIndex)
        }
    }



}
