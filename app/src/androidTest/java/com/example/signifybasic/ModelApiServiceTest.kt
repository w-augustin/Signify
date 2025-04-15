package com.example.signifybasic

import androidx.test.platform.app.InstrumentationRegistry
import com.example.signifybasic.signrecognition.ModelRetrofitClient
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
        val responseBody = JSONObject()
        responseBody.put("sign", "hello")
        responseBody.put("probability", "0.95")

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

        ModelRetrofitClient.BASE_URL = mockWebServer.url("/").toString()
        val service = ModelRetrofitClient.getInstance()

        val response = service.predict(videoPart, method).execute()

        Assert.assertTrue(response.isSuccessful)
        val body = response.body()?.string()
        val json = JSONObject(body!!)
        Assert.assertEquals("hello", json.getString("sign"))
        Assert.assertEquals("0.95", json.getString("probability"))
    }
}
