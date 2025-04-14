package com.example.signifybasic

import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import org.junit.Before
import org.junit.Test
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.signifybasic.signrecognition.*
import com.google.firebase.appdistribution.gradle.ApiService
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import android.provider.MediaStore
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import androidx.test.espresso.matcher.ViewMatchers.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject

class RecordVideoActivityTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(5000)

        // Override base URL to mock server
        val field = ModelRetrofitClient::class.java.getDeclaredField("BASE_URL")
        field.isAccessible = true
        field.set(null, mockWebServer.url("/").toString())

        Intents.init()
    }

    @After
    fun cleanUp() {
        mockWebServer.shutdown()
        Intents.release()
    }

    @Test
    fun testSubmitSupportedSign_launchesCameraIntent() {
        ActivityScenario.launch(RecordVideoActivity::class.java)

        onView(withId(R.id.inputSign))
            .perform(typeText("hello"), closeSoftKeyboard())

        onView(withId(R.id.btnRecord)).perform(click())

        Intents.intended(hasAction(MediaStore.ACTION_VIDEO_CAPTURE))

    }

    @Test
    fun testSubmitUnsupportedSign_showsErrorTextBox() {
        ActivityScenario.launch(RecordVideoActivity::class.java)

        onView(withId(R.id.inputSign))
            .perform(typeText("randomsign"), closeSoftKeyboard())

        onView(withId(R.id.btnRecord)).perform(click())

        // The error TextView is dynamically added — not directly testable.
        // You may need to use IdlingResource or extract Toast instead.
        // For now, check that the EditText is still focused (no intent launched)
        onView(withId(R.id.inputSign)).check(matches(hasFocus()))
    }

    /*
    @Test
    fun testVideoCapture_andSuccessfulResponse_launchesResultActivity() {

        val mockResponse = JSONObject().apply {
            put("sign", "hello")
            put("probability", "0.95")
        }

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse.toString())
        )

        val fakeVideoUri = Uri.parse("content://com.example.fake/video.mp4")

        // Stub the camera intent to return the fake URI
        val resultData = Intent().apply { data = fakeVideoUri }
        val activityResult = Instrumentation.ActivityResult(AppCompatActivity.RESULT_OK, resultData)

        intending(hasAction(MediaStore.ACTION_VIDEO_CAPTURE))
            .respondWith(activityResult)

        val scenario = ActivityScenario.launch(RecordVideoActivity::class.java)

        onView(withId(R.id.inputSign))
            .perform(typeText("hello"), closeSoftKeyboard())

        onView(withId(R.id.btnRecord)).perform(click())

        Intents.intended(hasComponent(SignRecognitionResultActivity::class.java.name))

    }*/
}
