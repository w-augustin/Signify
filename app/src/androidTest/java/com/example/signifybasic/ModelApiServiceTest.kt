package com.example.signifybasic

import androidx.test.platform.app.InstrumentationRegistry
import com.example.signifybasic.features.tabs.playground.videorecognition.ModelRetrofitClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.junit.*
import java.io.File

class ModelApiServiceTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testPredictApi_withMockResponse() {
        // 1. Create fake JSON response
        val responseBody = """
        [
            {"sign": "hello", "probability": 0.5},
            {"sign": "thank you", "probability": 0.3},
            {"sign": "bye", "probability": 0.2}
        ]
        """

        // 2. Enqueue fake response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody.toString())
                .addHeader("Content-Type", "application/json")
        )

        // 3. Create dummy file
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dummyFile = File.createTempFile("test_video", ".mp4", context.cacheDir)
        dummyFile.writeText("Fake video content")

        val mediaType = "video/mp4".toMediaTypeOrNull()
        val requestBody = dummyFile.asRequestBody(mediaType)
        val videoPart = MultipartBody.Part.createFormData("video", dummyFile.name, requestBody)

        // 4. Determine method type (alpha or holistic)
        val expectedSign = "hello" // Example sign
        val method = if (expectedSign.length == 1 && expectedSign.all { it.isLetter() }) "alpha" else "holistic"
        val expectedSignPart = MultipartBody.Part.createFormData("expected_sign", expectedSign)

        ModelRetrofitClient.BASE_URL = mockWebServer.url("/").toString()
        val service = ModelRetrofitClient.getInstance()

        val response = service.predict(videoPart, expectedSignPart, method).execute()

        Assert.assertTrue(response.isSuccessful)

        val predictions = response.body()
        Assert.assertNotNull(predictions)
        Assert.assertEquals(3, predictions!!.size)

        val first = predictions[0]
        val second = predictions[1]
        val third =  predictions[2]

        Assert.assertEquals("hello", first.sign)
        Assert.assertEquals(0.5, first.probability, 0.01)

        Assert.assertEquals("thank you", second.sign)
        Assert.assertEquals(0.3, second.probability, 0.01)

        Assert.assertEquals("bye", third.sign)
        Assert.assertEquals(0.2, third.probability, 0.01)
    }
}
