package com.example.signifybasic.features.tabs.playground.videorecognition

import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.HomePage
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.content.pm.PackageManager
import com.google.android.material.appbar.MaterialToolbar
import android.content.Context
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resumeWithException

class RecordVideoActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 101

    private lateinit var recordVideoBtn: Button
    private lateinit var inputEditText: AutoCompleteTextView
    private lateinit var expectedSign: String
    private lateinit var progressBar: ProgressBar
    private lateinit var backToHome: Button

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted. Try again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission is required to record video.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // generic xml setup
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video)

        recordVideoBtn = findViewById(R.id.btnRecord)
        inputEditText = findViewById(R.id.inputSign)
        progressBar = findViewById(R.id.progressBar)
        backToHome = findViewById(R.id.btnBack)

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        applyTextSizeToAllTextViews(rootView, this)
        if (isHighContrastEnabled(this)) {
            applyHighContrastToAllViews(rootView, this)
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val sizes = map?.getOutputSizes(Surface::class.java)
        val targetResolution = sizes?.find { it.width == 1280 && it.height == 720 }
            ?: sizes?.maxByOrNull { it.width * it.height }

        val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
        val targetFpsRange = fpsRanges?.find { it.lower == 30 && it.upper == 30 }
            ?: fpsRanges?.maxByOrNull { it.upper }

        if (targetResolution != null && targetFpsRange != null) {
            Log.d("CameraConfig", "Selected Resolution: ${targetResolution.width}x${targetResolution.height}")
            Log.d("CameraConfig", "Selected FPS Range: ${targetFpsRange.lower}-${targetFpsRange.upper}")
        } else {
            Log.w("CameraConfig", "Resolution or FPS not available!")
        }

        val support = listOf("hello", "hi", "thank you", "thanks", "book", "bye", "goodbye", "i love you") + ('a'..'z').map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, support)
        inputEditText.setAdapter(adapter)

        // Define a map for input phrases to their corresponding expected signs
        val inputMapping = mapOf(
            "hi" to "hello",
            "thanks" to "thank you",
            "goodbye" to "bye"
        )

        recordVideoBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                return@setOnClickListener
            }

            val preMappedSign = inputEditText.text.toString().trim().lowercase()

            // Check if the input is a mapped phrase
            expectedSign = inputMapping[preMappedSign] ?: preMappedSign

            if (expectedSign.isEmpty()) {
                showTextBoxUnderInput("Please enter a sign")
                return@setOnClickListener
            }

            if (!support.contains(expectedSign)) {
                showTextBoxUnderInput("Please enter a sign we support.\nRefer to the Dictionary section for valid signs.")
                return@setOnClickListener
            }

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent, 1)
        }

        backToHome.setOnClickListener {
            val intent = Intent(this@RecordVideoActivity, HomePage::class.java)
            startActivity(intent)
            finish()
        }
    }

    // render the predicted sign to text under the input
    private fun showTextBoxUnderInput(message: String) {
        val location = IntArray(2)
        inputEditText.getLocationOnScreen(location)

        val textView = TextView(this).apply {
            text = message
            setTextColor(getColor(R.color.light_red))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val parentLayout = findViewById<ViewGroup>(R.id.recordVideoLayout)
        parentLayout.addView(textView)
        textView.translationY = (location[1] + inputEditText.height - 50).toFloat()

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            parentLayout.removeView(textView)
        }
    }

    // when result is received, act accordingly
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val videoUri = data?.data
            if (videoUri == null) {
                Log.e("VideoError", "Video URI is null")
                Toast.makeText(this, "Error: Video URI is null", Toast.LENGTH_SHORT).show()
                return
            }

            val videoFile = createTempFileFromUri(videoUri)
            if (videoFile != null) {
                processVideoInBackground(videoFile, expectedSign)
            }
        } else {
            Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("video", ".mp4", cacheDir)

            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e("VideoError", "Failed to save video file", e)
            null
        }
    }

    private fun processVideoInBackground(videoFile: File, expectedSign: String) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val method =  if (expectedSign.length == 1 && expectedSign.all { it.isLetter() }) "alpha" else "holistic"

            // For testing purposes, fetch video from assets instead of using the camera/video capture.
            // val videoFileFromAssets = getVideoFromAssets(applicationContext, "bye.mp4")

            val result = recognizeSign(videoFile, method, expectedSign)

            val predictionsList = mutableListOf<String>()

            var sign = "Unknown"
            var score = 0.0

            try {
                // Process the list of predictions
                if (result.isNotEmpty()) {
                    result.forEach { prediction ->
                        sign = prediction.sign ?: "Unknown"
                        score = prediction.probability ?: 0.0
                        predictionsList.add("$sign: ${(score * 100).toInt()}%")
                    }
                } else {
                    Log.e("API Error", "Unexpected response: $result")
                }
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing response", e)
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                val intent = Intent(this@RecordVideoActivity, SignRecognitionResultActivity::class.java)
                intent.putStringArrayListExtra("predictionsList", ArrayList(predictionsList))
                intent.putExtra("inputtedSign", expectedSign)
                startActivity(intent)
            }
        }
    }

    private suspend fun recognizeSign(videoFile: File, method: String, expectedSign: String): List<Prediction> {
        val mediaType = "video/mp4".toMediaType()
        val requestBody = videoFile.asRequestBody(mediaType)
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, requestBody)
        val expectedSignPart = MultipartBody.Part.createFormData("expectedSign", expectedSign)


        return suspendCoroutine { continuation ->
            ModelRetrofitClient.getInstance().predict(videoPart, expectedSignPart, method)
                .enqueue(object : Callback<List<Prediction>>  {
                    override fun onResponse(call: Call<List<Prediction>>, response: Response<List<Prediction>>) {
                        if (response.isSuccessful) {
                            val predictions = response.body() ?: emptyList()
                            continuation.resume(predictions)  // Returning the list of predictions
                        } else {
                            continuation.resumeWithException(Exception("Failed to recognize sign: ${response.message()}"))
                        }
                    }

                    override fun onFailure(call: Call<List<Prediction>>, t: Throwable) {
                        continuation.resumeWithException(Exception("Network failure: ${t.message}"))
                    }
                })
        }
    }

    fun setExpectedSignForTest(value: String) {
        expectedSign = value
    }

    /*suspend fun getVideoFromAssets(context: Context, videoFileName: String): File? {
        val assetManager = context.assets
        val videoFile = File(context.cacheDir, videoFileName)

        try {
            // Open the video file from assets
            val inputStream: InputStream = assetManager.open(videoFileName)
            val outputStream = FileOutputStream(videoFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            // Close the streams
            inputStream.close()
            outputStream.close()

            return videoFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }*/

}
