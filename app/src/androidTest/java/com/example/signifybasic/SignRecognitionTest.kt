package com.example.signifybasic

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import okhttp3.mockwebserver.MockWebServer
import android.provider.MediaStore
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.rule.GrantPermissionRule
import org.junit.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import com.example.signifybasic.signrecognition.videorecognition.ModelRetrofitClient
import com.example.signifybasic.signrecognition.videorecognition.RecordVideoActivity

class RecordVideoActivityTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        Intents.init()

        mockWebServer = MockWebServer()
        mockWebServer.start(5000)

        // Override base URL to mock server
        val field = ModelRetrofitClient::class.java.getDeclaredField("BASE_URL")
        field.isAccessible = true
        field.set(null, mockWebServer.url("/").toString())
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

        // check that the EditText is still focused (no intent launched)
        onView(withId(R.id.inputSign)).check(matches(hasFocus()))
    }
}
