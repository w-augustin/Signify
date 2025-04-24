package com.example.signifybasic

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.example.signifybasic.features.tabs.playground.videorecognition.SignRecognitionResultActivity
import org.junit.Before
import org.junit.Test
import androidx.test.espresso.intent.Intents
import org.hamcrest.CoreMatchers.containsString
import org.junit.After

class SignRecognitionResultActivityTest {

    @Before
    fun setUp() {
        // Initialize Intents before each test
        Intents.init()
    }

    @Test
    fun testSignRecognitionResultActivity_DisplaysResultsAndHandlesButtonsCorrect() {
        // Prepare the Intent with test data
        val intent = Intent(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            SignRecognitionResultActivity::class.java
        ).apply {
            putExtra("predictionsList", arrayListOf("hello: 0.95", "thank you: 0.85"))  // or however you format
            putExtra("inputtedSign", "hello")
        }

        // Launch the activity with the Intent
        ActivityScenario.launch<SignRecognitionResultActivity>(intent)

        // Check if the TextViews are displaying the correct data
        onView(withId(R.id.predictionsTextView))
            .check(matches(withText(containsString("hello: 0.95"))))

        onView(withId(R.id.inputtedSignTextView))
            .check(matches(withText(containsString("You signed: hello"))))


        // Test the "Record Another Video" button click behavior
        onView(withId(R.id.btnRecordAnother)).perform(click())
    }

    @Test
    fun testSignRecognitionResultActivity_DisplaysResultsAndHandlesButtonsIncorrect() {
        // Prepare the Intent with test data
        val intent = Intent(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            SignRecognitionResultActivity::class.java
        ).apply {
            putExtra("predictionsList", arrayListOf("hello: 0.95", "bye: 0.85"))  // or however you format
            putExtra("inputtedSign", "book")
        }

        // Launch the activity with the Intent
        ActivityScenario.launch<SignRecognitionResultActivity>(intent)

        // Check if the TextViews are displaying the correct data
        onView(withId(R.id.predictionsTextView))
            .check(matches(withText(containsString("bye: 0.85"))))

        onView(withId(R.id.inputtedSignTextView))
            .check(matches(withText(containsString("You signed: book"))))


        // Test the "Record Another Video" button click behavior
        onView(withId(R.id.btnRecordAnother)).perform(click())
    }

    @After
    fun tearDown() {
        // Release resources after each test
        Intents.release()
    }
}
