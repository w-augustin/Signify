package com.example.signifybasic.signrecognition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
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
import kotlinx.coroutines.*
import kotlin.coroutines.resume


class TakePictureActivity : AppCompatActivity() {
    // creating variables on below line.
    private lateinit var  recordVideoBtn: Button
    private lateinit var  videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_picture)

        // initializing variables
        recordVideoBtn = findViewById(R.id.idBtnRecordVideo)
        videoView = findViewById(R.id.videoView)

        recordVideoBtn.setOnClickListener { //capture a video.
            val i = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            // an activity for result.
            startActivityForResult(i, 1)
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
                processVideoInBackground(videoFile)
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
    private fun processVideoInBackground(videoFile: File) {
        // Start a coroutine to process the video in the background
        CoroutineScope(Dispatchers.IO).launch {
            val result = recognizeSign(videoFile)

            // Update UI after the background work is done
            withContext(Dispatchers.Main) {
                val recognizedSignTextView = findViewById<TextView>(R.id.tvRecognizedSign)
                recognizedSignTextView.text = "Recognized Sign: $result"
            }
        }
    }

    private suspend fun recognizeSign(videoFile: File): String {

        val mediaType = "video/mp4".toMediaType()
        val requestBody = videoFile.asRequestBody(mediaType)
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, requestBody)

        // Perform network request on the background thread
        return suspendCancellableCoroutine { continuation ->
            ModelRetrofitClient.apiService.predict(videoPart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            continuation.resume(result ?: "Failed to recognize sign") {
                                // Handle cancellation if needed
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
}