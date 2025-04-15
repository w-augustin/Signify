package com.example.signifybasic.games

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.activitycenter.ActivityCenter
import com.example.signifybasic.features.tabs.playground.videorecognition.ModelRetrofitClient
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

class SigningGameActivity : BaseGameActivity() {

    override fun getGameLayoutId(): Int = R.layout.activity_signing_game

    private lateinit var promptTextView: TextView
    private lateinit var recordButton: Button
    private lateinit var continueButton: Button
    private lateinit var loadingProgressBar: ProgressBar

    private lateinit var expectedSign: String
    private lateinit var resultKey: String
    private var stepIndex: Int = -1
    private val REQUEST_VIDEO_CAPTURE = 123

    private var failedAttempts = 0
    private var recognizedSign: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepIndex = intent.getIntExtra("STEP_INDEX", -1)
        val step = GameSequenceManager.sequence.getOrNull(stepIndex)

        if (step == null || step.type != "signing") {
            Toast.makeText(this, "Error loading signing step", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        expectedSign = step.expectedSign ?: ""
        resultKey = step.resultKey ?: "SIGNING_BOOL"

        promptTextView = findViewById(R.id.prompt)
        recordButton = findViewById(R.id.record_button)
        continueButton = findViewById(R.id.continue_button)
        loadingProgressBar = findViewById(R.id.progress_bar)

        promptTextView.text = step.prompt ?: "Sign the correct word"

        continueButton.isEnabled = false
        continueButton.alpha = 0.5f

        recordButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_VIDEO_CAPTURE)
            } else {
                launchCamera()
            }
        }

        continueButton.setOnClickListener {
            val intent = Intent(this, ActivityCenter::class.java)

            val currentModule = ModuleManager.getModules()[ModuleManager.currentModuleIndex]
            val isLastStep = ModuleManager.currentStepIndex >= currentModule.games.size

            if (!isLastStep) {
                // Only set CONTINUE_SEQUENCE if there's more to do
                intent.putExtra("CONTINUE_SEQUENCE", true)
            }

            intent.putExtra("IS_CORRECT", true)
            startActivity(intent)
            finish()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_VIDEO_CAPTURE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                createTempFileFromUri(uri)?.let { file ->
                    processVideo(file)
                } ?: Toast.makeText(this, "Could not process video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("video_", ".mp4", cacheDir)
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (input.read(buffer).also { len = it } > 0) {
                        output.write(buffer, 0, len)
                    }
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("VideoError", "Failed to create file", e)
            null
        }
    }

    private fun processVideo(file: File) {
        loadingProgressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val result = recognizeSign(file)

            recognizedSign = try {
                if (result.startsWith("{")) JSONObject(result).optString("sign", "Unknown")
                else "Unknown"
            } catch (e: Exception) {
                Log.e("Parsing", "Error parsing result", e)
                "Unknown"
            }

            val isMatch = recognizedSign.equals(expectedSign, ignoreCase = true)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                if (isMatch) {
                    Toast.makeText(this@SigningGameActivity, "Correct! You signed: $recognizedSign", Toast.LENGTH_SHORT).show()
                    enableContinue()
                } else {
                    failedAttempts++
                    if (failedAttempts >= 2) {
                        Toast.makeText(this@SigningGameActivity, "Letting you continue after 2 attempts. You signed: $recognizedSign", Toast.LENGTH_LONG).show()
                        enableContinue()
                    } else {
                        Toast.makeText(this@SigningGameActivity, "Incorrect. You signed: $recognizedSign. Try again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun enableContinue() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "admin") ?: "admin"

        ModuleManager.moveToNextStep()
        val modIndex = ModuleManager.currentModuleIndex
        val stepIndex = ModuleManager.currentStepIndex

        DBHelper(this).updateUserProgress(username, modIndex, stepIndex)
        Log.d("PROGRESS", "SigningGame saved: module=$modIndex, step=$stepIndex")

        continueButton.isEnabled = true
        continueButton.alpha = 1f
    }


    private suspend fun recognizeSign(file: File): String {
        val mediaType = "video/mp4".toMediaType()
        val body = file.asRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData("video", file.name, body)

        return suspendCoroutine { cont ->
            ModelRetrofitClient.getInstance().predict(part).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    cont.resume(response.body()?.string() ?: "")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    cont.resume("Network error: ${t.message}")
                }
            })
        }
    }
}
