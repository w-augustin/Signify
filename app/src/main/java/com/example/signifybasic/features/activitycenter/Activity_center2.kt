//package com.example.signifybasic.features.activitycenter
//
//import android.os.Bundle
//import android.util.Log
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.signifybasic.R
//import com.google.android.material.appbar.MaterialToolbar
//import com.example.signifybasic.games.GameRouter
//import com.example.signifybasic.games.GameSequenceManager
//import com.example.signifybasic.games.ModuleManager
//import com.example.signifybasic.features.utility.applyHighContrastToAllViews
//import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
//import com.example.signifybasic.features.utility.isHighContrastEnabled
//
//
class activity_center2 : AppCompatActivity() {
//
//    private var gameSequenceInitialized = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_center2)
//        val rootView = findViewById<ViewGroup>(android.R.id.content)
//        applyTextSizeToAllTextViews(rootView, this)
//        if (isHighContrastEnabled(this)) {
//            applyHighContrastToAllViews(rootView, this)
//        }
//
//        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
//            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
//            v.setPadding(0, topInset, 0, 0)
//            insets
//        }
//        toolbar.setNavigationOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//        //loading in activities from activity center 1 needs to be update
//        val getStartedButton = findViewById<Button>(R.id.button2)
//        getStartedButton.setOnClickListener {
//            ModuleManager.loadModules(this)
//
//            //not working modules are not loading
//            if (ModuleManager.currentModuleIndex != 2) {
//                ModuleManager.resetModule(2)
//            }
//            Log.d("ModuleManager", "Starting at Module: ${ModuleManager.currentModuleIndex}, Step: ${ModuleManager.currentStepIndex}")
//
//
//            val firstStep = ModuleManager.getCurrentStep()
//            if (firstStep != null) {
//                GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
//            } else {
//                Toast.makeText(this, "Module is empty", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        if (!gameSequenceInitialized) {
//            GameSequenceManager.load(this)
//            gameSequenceInitialized = true
//        }
//
//        val continueFromSequence = intent.getBooleanExtra("CONTINUE_SEQUENCE", false)
//        if (continueFromSequence) {
//            val nextStep = ModuleManager.moveToNextStep()
//
//            if (nextStep != null) {
//                GameRouter.routeToGame(this, ModuleManager.currentStepIndex)
//            } else if (!ModuleManager.isAllModulesComplete()) {
//                Toast.makeText(this, "Module complete! ", Toast.LENGTH_SHORT).show()
//
//            } else {
//                Toast.makeText(this, "All modules complete!", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
}
//
//
//
