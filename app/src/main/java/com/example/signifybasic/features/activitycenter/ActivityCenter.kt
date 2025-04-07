package com.example.signifybasic.features.activitycenter

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.example.signifybasic.fill_blank_game
import com.example.signifybasic.getStarted
import com.example.signifybasic.identify_game
import com.example.signifybasic.identify_game2
import com.example.signifybasic.matching_game
import com.example.signifybasic.matching_game2
import com.example.signifybasic.selecting_game
import com.example.signifybasic.selecting_game2
import com.example.signifybasic.signing_game
import com.google.android.material.appbar.MaterialToolbar


class ActivityCenter : AppCompatActivity() {
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

        val activitySelect= findViewById<Button>(R.id.button2)
        val activityIdentify = findViewById<Button>(R.id.button3)
        activityIdentify.isActivated = false
        val activityFillInBlank = findViewById<Button>(R.id.button4)
        activityFillInBlank.isActivated = false
        val activityMatching= findViewById<Button>(R.id.button5)
        activityMatching.isActivated =false
        val activitySigning= findViewById<Button>(R.id.button11)
        activitySigning.isActivated =false
        val activityMatching2 =findViewById<Button>(R.id.button10)
        activityMatching2.isActivated =false
        val activityIdentify2 =findViewById<Button>(R.id.button6)
        activityIdentify2.isActivated = false
        val activitySelecting2= findViewById<Button>(R.id.button7)
        activitySelecting2.isActivated =false
        val getStarted = findViewById<Button>(R.id.getStarted)
        getStarted.isActivated =false

        var currProgress: Int =0;
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            // boolean check if each activity is completed correctly
        val isCorrect = intent.getBooleanExtra("IS_CORRECT", false)
        val identify_bool =intent.getBooleanExtra("IDENTIFY_BOOL", false)
        val fill_blank_bool= intent.getBooleanExtra("FILL_BLANK_BOOL",false)
        val matching_bool = intent.getBooleanExtra("MATCHING_BOOL",false)
        val identify2_bool = intent.getBooleanExtra("IDENTIFY2_BOOL",false)
        val selecting2_bool = intent.getBooleanExtra("SELECTING2_BOOL",false)
        val matching2_bool = intent.getBooleanExtra("MATCHING2_BOOL",false)
        val dbHelper = DBHelper(this)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null)

        progressBar.max = 100

        fun increaseProgressBar() {
            currProgress = currProgress + 14

            if(currProgress > progressBar.max){
                currProgress = progressBar.max
            }
            progressBar.setProgress(currProgress)

            //dbHelper.addUserProgress(username,activitySelect.id,isCorrect,currProgress)

        }

        // function to update the UI of each activity
        fun activityIndUiUpdate(){
            activityIdentify.isActivated = true
            activityIdentify.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))

            activityIdentify.setOnClickListener {
                startActivity( Intent(this,identify_game::class.java))
            }
        }
        fun activityFillBlankUiUpdate(){
            activityFillInBlank.isActivated = true
            activityFillInBlank.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
            activityFillInBlank.setOnClickListener {
                startActivity( Intent(this,fill_blank_game::class.java))
            }
        }
        fun UiUpdateMatching(){
            activityMatching.isActivated =true
            activityMatching.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_blue))
            activityMatching.setOnClickListener {
                startActivity(Intent(this,matching_game::class.java))
            }
        }
        fun UiUpdateMatching2(){
            activityMatching2.isActivated =true
            activityMatching2.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_blue))
            activityMatching2.setOnClickListener {
                startActivity(Intent(this,matching_game2::class.java))
            }
        }
        fun UiUpdateIdentify2(){
            activityIdentify2.isActivated =true
            activityIdentify2.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_blue))
            activityIdentify2.setOnClickListener {
                startActivity(Intent(this,identify_game2::class.java))
            }
        }
        fun UiUpdateSelecting2(){
            activitySelecting2.isActivated =true
            activitySelecting2.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_blue))
            activitySelecting2.setOnClickListener {
                startActivity(Intent(this,selecting_game2::class.java))
            }
        }
        fun UiUpdateSigning(){
            activitySigning.isActivated =true
            activitySigning.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_blue))
            activitySigning.setOnClickListener {
                startActivity(Intent(this,signing_game::class.java))
            }
        }

        getStarted.setOnClickListener {
            startActivity(Intent(this,getStarted::class.java))
        }
        activitySelect.setOnClickListener{
                startActivity(Intent(this, selecting_game::class.java))
            }

            if(isCorrect){
               increaseProgressBar()
                activityIdentify.isActivated = true
                activityIdentify.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))

                activityIdentify.setOnClickListener {
                    startActivity( Intent(this,identify_game::class.java))
                  //  increaseProgressBar()

                }
            }

        if(identify_bool){
            activityIndUiUpdate()
            increaseProgressBar()
            increaseProgressBar()
            activityFillBlankUiUpdate()
        }
        if(fill_blank_bool){
            activityIndUiUpdate()
            activityFillBlankUiUpdate()
               increaseProgressBar()
                increaseProgressBar()
                increaseProgressBar()
            UiUpdateMatching()

        }
        if(matching_bool){
            activityIndUiUpdate()
            activityFillBlankUiUpdate()
            UiUpdateMatching()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            UiUpdateIdentify2()
        }
        if(identify2_bool){
            activityIndUiUpdate()
            activityFillBlankUiUpdate()
            UiUpdateMatching()
            UiUpdateIdentify2()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            UiUpdateSelecting2()
        }
        if(selecting2_bool){
            activityIndUiUpdate()
            activityFillBlankUiUpdate()
            UiUpdateMatching()
            UiUpdateIdentify2()
            UiUpdateSelecting2()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            UiUpdateMatching2()

        }
        if(matching2_bool){
            activityIndUiUpdate()
            activityFillBlankUiUpdate()
            UiUpdateMatching()
            UiUpdateIdentify2()
            UiUpdateSelecting2()
            UiUpdateMatching2()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            increaseProgressBar()
            UiUpdateSigning()
            
        }

    }
}
