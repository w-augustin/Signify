package com.example.signifybasic.signrecognition

import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.widget.EditText
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import com.example.signifybasic.R
import com.example.signifybasic.features.tabs.HomePage
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import android.content.pm.PackageManager

class RecordVideoActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 101

    // creating variables on below line.
    private lateinit var  recordVideoBtn: Button
    private lateinit var  backBtn: Button
    private lateinit var inputEditText: AutoCompleteTextView
    private lateinit var expectedSign: String
    private lateinit var progressBar: ProgressBar

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Optional: re-trigger the record logic here
                Toast.makeText(this, "Camera permission granted. Try again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission is required to record video.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video)

        // initializing variables
        recordVideoBtn = findViewById(R.id.btnRecord)
        backBtn = findViewById(R.id.btnBack)
        inputEditText = findViewById(R.id.inputSign)
        progressBar = findViewById(R.id.progressBar)

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Use first camera (back camera)

        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        // Ensure resolution is available
        val sizes = map?.getOutputSizes(Surface::class.java)
        val targetResolution = sizes?.find { it.width == 1280 && it.height == 720 }
            ?: sizes?.maxByOrNull { it.width * it.height } // Choose highest available

        // Get available FPS ranges
        val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
        val targetFpsRange = fpsRanges?.find { it.lower == 30 && it.upper == 30 }
            ?: fpsRanges?.maxByOrNull { it.upper } // Choose highest available if 30 is not supported

        if (targetResolution != null && targetFpsRange != null) {
            Log.d("CameraConfig", "Selected Resolution: ${targetResolution.width}x${targetResolution.height}")
            Log.d("CameraConfig", "Selected FPS Range: ${targetFpsRange.lower}-${targetFpsRange.upper}")
        } else {
            Log.w("CameraConfig", "Resolution or FPS not available!")
        }

        val support = listOf(
            "hello", "hi", "thank you", "thanks", "name", "book", "bye", "goodbye", "i love you"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, support)
        inputEditText.setAdapter(adapter)

recordVideoCard.setOnClickListener {
    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        return@setOnClickListener
    }

    expectedSign = inputEditText.text.toString().trim().lowercase()

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

backBtn.setOnClickListener {
    val intent = Intent(this, HomePage::class.java)
    startActivity(intent)
    finish()
}

    // Helper function to create and show a TextBox under the input field
    private fun showTextBoxUnderInput(message: String) {
        // Calculate the position of the inputEditText view
        val location = IntArray(2)
        inputEditText.getLocationOnScreen(location)

        // Create the TextView (acting as a custom Toast)
        val textView = TextView(this)
        textView.text = message
        textView.setTextColor(getColor(R.color.red)) // Set text color (can be changed)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f) // Set text size
        textView.gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally

        // Layout parameters to place the TextView below the inputEditText
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        textView.layoutParams = layoutParams
        val parentLayout = findViewById<ViewGroup>(R.id.recordVideoLayout) // Ensure you have a parent layout for this TextView
        parentLayout.addView(textView)

        // Position the TextView below the input field
        textView.translationY = (location[1] + inputEditText.height - 50).toFloat()

        // Hide the TextBox after 3 seconds (or as needed)
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            parentLayout.removeView(textView) // Remove the TextView after delay
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val videoUri = data?.data
            if (videoUri == null) {
                Log.e("VideoError", "Video URI is null")
                Toast.makeText(this, "Error: Video URI is null", Toast.LENGTH_SHORT).show()
            }

            // Create a temporary file to save the video
            val videoFile = createTempFileFromUri(videoUri!!)

            if (videoFile != null) {
                // Launch background processing of the video
                processVideoInBackground(videoFile, expectedSign)
            }
        } else {
            Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            // Open the video URI and copy it to a temporary file
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

            inputStream?.close()
            tempFile
        } catch (e: Exception) {
            Log.e("VideoError", "Failed to save video file", e)
            null
        }
    }

    // Function to process video in a background thread
    private fun processVideoInBackground(videoFile: File, expectedSign : String) {

        // Show the ProgressBar while the video is being processed
        progressBar.visibility = View.VISIBLE

        // Start a coroutine to process the video in the background
        CoroutineScope(Dispatchers.IO).launch {
            val result = recognizeSign(videoFile)

            // Extracting sign and probability separately
            var sign = "Unknown"
            var score = "0.0"

            try {
                // Check if the response is valid JSON
                if (result.startsWith("{")) {
                    val json = JSONObject(result)
                    sign = json.optString("sign", "Unknown")
                    score = json.optString("probability", "0.0")
                } else {
                    Log.e("API Error", "Unexpected response: $result")
                }
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing response", e)
            }

            // Update UI after the background work is done
            withContext(Dispatchers.Main) {
                val isMatch = sign.lowercase() == expectedSign.lowercase()

                val message = if (isMatch) {
                    "Match! You signed: $sign"
                } else {
                    "No match.\nYou signed: $sign\nExpected: $expectedSign"
                }

                // Hide the progress bar once the result is processed
                progressBar.visibility = View.GONE

                // Instead of updating a TextView in this activity, start the new activity and pass data
                val intent = Intent(this@RecordVideoActivity, SignRecognitionResultActivity::class.java)
                intent.putExtra("recognizedSign", sign) // Pass recognized sign as String
                intent.putExtra("score", score) // Pass score as Double
                intent.putExtra("matchResult", message)
                startActivity(intent)

                // finish() // Finish current activity to prevent user from going back
                //val recognizedSignTextView = findViewById<TextView>(R.id.tvRecognizedSign)
                //recognizedSignTextView.text = "Recognized Sign: $result"
            }
        }
    }

    private suspend fun recognizeSign(videoFile: File): String {

        val mediaType = "video/mp4".toMediaType()
        val requestBody = videoFile.asRequestBody(mediaType)
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, requestBody)

        // Perform network request on the background thread
        return suspendCancellableCoroutine { continuation ->
            ModelRetrofitClient.getInstance().predict(videoPart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            if (result != null) {
                                Log.d("API Response", result)
                                continuation.resume("$result") // Return sign and probability as percentage
                            } else {
                                continuation.resume("Failed to recognize sign")
                            }
                        } else {
                            continuation.resume("Server error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resume("Network failure: ${t.message}")
                    }
                })
        }
    }

    // Only for testing
    fun setExpectedSignForTest(value: String) {
        expectedSign = value
    }
}