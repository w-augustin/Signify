package com.example.signifybasic

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.example.signifybasic.signrecognition.videorecognition.SignRecognitionResultActivity
import org.junit.Before
import org.junit.Test
import androidx.test.espresso.intent.Intents
import org.junit.After

class SignRecognitionResultActivityTest {

    @Before
    fun setUp() {
        // Initialize Intents before each test
        Intents.init()
    }

    @Test
    fun testSignRecognitionResultActivity_DisplaysResultsAndHandlesButtons() {
        // Prepare the Intent with test data
        val intent = Intent(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            SignRecognitionResultActivity::class.java
        ).apply {
            putExtra("recognizedSign", "hello")
            putExtra("score", "0.95")
            putExtra("matchResult", "Match! You signed: hello")
        }

        // Launch the activity with the Intent
        ActivityScenario.launch<SignRecognitionResultActivity>(intent)

        // Check if the TextViews are displaying the correct data
        onView(withId(R.id.tvRecognizedSign))
            .check(matches(withText("Recognized Sign: hello")))

        onView(withId(R.id.tvScore))
            .check(matches(withText("Confidence: 0.95")))

        onView(withId(R.id.tvMatchResult))
            .check(matches(withText("Match! You signed: hello")))

        // Test the "Record Another Video" button click behavior
        onView(withId(R.id.btnRecordAnother)).perform(click())
    }

    @After
    fun tearDown() {
        // Release resources after each test
        Intents.release()
    }
}
