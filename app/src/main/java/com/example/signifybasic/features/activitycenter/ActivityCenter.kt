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
import com.example.signifybasic.games.ModuleManager
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.tabs.HomePage
import com.example.signifybasic.games.ModuleManager.currentStepIndex


class ActivityCenter : AppCompatActivity() {

    // find the furthest unlocked module
    fun getMaxUnlockedModuleIndex(context: Context): Int {
        // get full list of gameModules
        val modules = ModuleManager.getModules()

        // get user's furthest completed module and activity
        val dbHelper = DBHelper(context)
        val username = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getString("loggedInUser", "admin") ?: "admin"
        val (modIndex, stepIndex) = dbHelper.getUserProgress(username)

        return if (
            modIndex < modules.size &&
            stepIndex >= modules[modIndex].games.size - 1
        ) {
            // current module fully completed + there's a further module => next module unlocked
            (modIndex + 1).coerceAtMost(modules.size - 1)
        } else {
            // all modules completed / current module not completed => current module is max module.
            modIndex
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // setting basic xml
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
            val intent = Intent(this, HomePage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // setting state values of page
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"
        val dbHelper = DBHelper(this)
        ModuleManager.loadModules(this)

        // when user opens activity center, put them at their max progress point automatically
        if (!intent.getBooleanExtra("FORCE_OVERRIDE", false)) {
            Log.d("PROGRESS", "AUTO PLACING USER")
            val (modIndex, stepIndex) = dbHelper.getUserProgress(username)
            ModuleManager.currentModuleIndex = modIndex
            ModuleManager.currentStepIndex = stepIndex
        }

        // set up buttons and progress using module state information
        val (maxModIndex, maxStepIndex) = dbHelper.getUserProgress(username)
        setupButtons(ModuleManager.currentModuleIndex, maxModIndex, maxStepIndex)
        updateProgressBar(findViewById(R.id.progressBar), ModuleManager.currentModuleIndex, maxModIndex, maxStepIndex)

        // if we are continuing to the next activity in the sequence
        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
        if (continueFromSequence) {
            Log.d("DEBUG_FLOW", "continueFromSequence triggered")
            Log.d("DEBUG_FLOW", "Step index: ${ModuleManager.currentStepIndex}")

            val module = ModuleManager.getModules().getOrNull(ModuleManager.currentModuleIndex)
            val hasFinishedModule = module == null || ModuleManager.currentStepIndex >= module.games.size
            if (module != null) {
                Log.d("DEBUG_FLOW", "${ModuleManager.currentStepIndex}, and ${module.games.size}")
            }

            // if user completed last activity in module, don't continue
            if (hasFinishedModule) {
                Toast.makeText(this, "You've completed all activities in this module!", Toast.LENGTH_SHORT).show()
                return
            }

            val step = ModuleManager.getCurrentStep()
            if (step != null) {
                val options = android.app.ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                GameRouter.routeToGame(this, ModuleManager.currentStepIndex, options.toBundle())
            } else {
                Toast.makeText(this, "No more steps found.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val currentModule = ModuleManager.getModules().getOrNull(ModuleManager.currentModuleIndex)
        val totalSteps = currentModule?.games?.size ?: 0

        // Get user's saved progress (NOT just the currently selected step)
        val (progressModuleIndex, progressStepIndex) = dbHelper.getUserProgress(username)

        val moduleComplete = ModuleManager.currentModuleIndex < progressModuleIndex || (
                ModuleManager.currentModuleIndex == progressModuleIndex &&
                        progressStepIndex >= totalSteps
                )

        // if user completed the module, make the necessary updates to the user's account
        if (moduleComplete) {
            val userId = dbHelper.getUserIdByUsername(username)
            val currentModIndex = ModuleManager.currentModuleIndex
            val totalModules = ModuleManager.getModules().size

            if (userId != null) {
                when (currentModIndex) {
                    0 -> dbHelper.addAchievement(userId, "Getting Started", this)
                    1 -> dbHelper.addAchievement(userId, "Module 2", this)
                }

                // if all modules + steps completed
                val (savedModuleIndex, savedStepIndex) = dbHelper.getUserProgress(username)
                val lastModule = ModuleManager.getModules().lastOrNull()
                val allModulesComplete = savedModuleIndex >= totalModules - 1 &&
                        lastModule != null &&
                        savedStepIndex >= lastModule.games.size

                if (allModulesComplete) {
                    dbHelper.addAchievement(userId, "Module Master", this)
                }
            }

        }

        val nextModuleButton = findViewById<Button>(R.id.nextMod)
        nextModuleButton.isEnabled = moduleComplete
        nextModuleButton.alpha = if (moduleComplete) 1f else 0.5f

        nextModuleButton.setOnClickListener {
            val nextIndex = ModuleManager.currentModuleIndex + 1
            val maxAllowed = getMaxUnlockedModuleIndex(this)

            if (nextIndex <= maxAllowed && nextIndex < ModuleManager.getModules().size) {
                // Start from the beginning of the next module
                val intent = Intent(this, ActivityCenter::class.java)
                intent.putExtra("FORCE_MODULE_INDEX", nextIndex)
                intent.putExtra("FORCE_STEP_INDEX", 0) // start from beginning
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
        val moduleAdapter = ArrayAdapter(this, R.layout.spinner_item_white, moduleTitles)
        moduleAdapter.setDropDownViewResource(R.layout.spinner_item_black)
        moduleSpinner.adapter = moduleAdapter

        // Set currently selected module
        moduleSpinner.setSelection(ModuleManager.currentModuleIndex)

        // allow user to switch between unlocked modules
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

                    setupButtons(currentModuleIndex = ModuleManager.currentModuleIndex, userProgressModule = maxModIndex, userProgressStep = maxStepIndex)
                    updateProgressBar(findViewById(R.id.progressBar), ModuleManager.currentModuleIndex, maxModIndex, maxStepIndex)
                } else {
                    Toast.makeText(this@ActivityCenter, "This module is locked!", Toast.LENGTH_SHORT).show()
                    moduleSpinner.setSelection(ModuleManager.currentModuleIndex)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // programmatically generate all the buttons for the currently loaded module
    private fun setupButtons(currentModuleIndex: Int, userProgressModule: Int, userProgressStep: Int) {
        val module = ModuleManager.getModules()[currentModuleIndex]
        val density = resources.displayMetrics.density
        val screenWidth = resources.displayMetrics.widthPixels
        val usableWidth = screenWidth - 2 * (50 * density).toInt()
        val buttonSize = (90 * density).toInt()
        val paddingLimit = (50 * density).toInt()

        val container = findViewById<LinearLayout>(R.id.gameButtonContainer)
        container.removeAllViews()

        // iterate over each game in the module
        for (i in module.games.indices) {
            // is a button enabled?
            val isEnabled = when {
                currentModuleIndex < userProgressModule -> true // if user has progressed to another module => auto yes
                currentModuleIndex == userProgressModule -> i <= userProgressStep // if user has progressed to this button in the module => yes
                else -> false
            }

            val button = Button(this).apply {
                text = "M${currentModuleIndex + 1}" + "A${i + 1}"
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
                    ModuleManager.currentStepIndex = i
                    GameRouter.routeToGame(this@ActivityCenter, i)
                }
            }

            container.addView(button)
        }
    }

    // dynamically update the progress bar according to user's completion rate
    private fun updateProgressBar(progressBar: ProgressBar, currentModuleIndex: Int, userProgressModule: Int, userProgressStep: Int) {
        val currentModule = ModuleManager.getModules()[currentModuleIndex]
        val totalSteps = currentModule.games.size

        // find step progress in this module
        val completedSteps = when {
            userProgressModule > currentModuleIndex -> totalSteps // module must be completed.
            userProgressModule == currentModuleIndex -> userProgressStep // 0-based index
            else -> 0 // module locked => 0 progress
        }

        val progress = ((completedSteps.toFloat() / totalSteps.toFloat()) * 100).toInt()
        progressBar.progress = progress

        Log.d("ProgressTracking", "Module $currentModuleIndex: $completedSteps / $totalSteps → $progress%")
    }

    // save progress for user
    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

        val dbHelper = DBHelper(this)
        val (maxModIndex, maxStepIndex) = dbHelper.getUserProgress(username)

        // Load modules (if needed)
        ModuleManager.loadModules(this)

        // Check if intent is forcing a module switch
        val forcedModuleIndex = intent.getIntExtra("FORCE_MODULE_INDEX", -1)
        val forcedStepIndex = intent.getIntExtra("FORCE_STEP_INDEX", -1)

        if (forcedModuleIndex != -1 && forcedStepIndex != -1) {
            ModuleManager.currentModuleIndex = forcedModuleIndex
            ModuleManager.currentStepIndex = forcedStepIndex
            Log.d("DEBUG_FLOW", "FORCED to mod=$forcedModuleIndex step=$forcedStepIndex")
        } else {
            val (savedModuleIndex, savedStepIndex) = dbHelper.getUserProgress(username)
            ModuleManager.currentModuleIndex = savedModuleIndex
            ModuleManager.currentStepIndex = savedStepIndex
            Log.d("DEBUG_FLOW", "Reloaded from DB: mod=$savedModuleIndex step=$savedStepIndex")
        }

        // Logging final resolved values
        Log.d(
            "DEBUG_FLOW",
            "Final resolved: module=${ModuleManager.currentModuleIndex} step=${ModuleManager.currentStepIndex}"
        )

        // Refresh UI based on final resolved state
        updateProgressBar(findViewById(R.id.progressBar), ModuleManager.currentModuleIndex, maxModIndex, maxStepIndex)
        setupButtons(currentModuleIndex = ModuleManager.currentModuleIndex, userProgressModule = maxModIndex, userProgressStep = maxStepIndex)

        // Optional: clear intent extras after use
        intent.removeExtra("FORCE_MODULE_INDEX")
        intent.removeExtra("FORCE_STEP_INDEX")
    }
}
