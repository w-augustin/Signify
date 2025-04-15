package com.example.signifybasic.features.tabs.playground.liverecognition

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.signifybasic.databinding.ActivityLiveSignRecognitionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LiveSignRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveSignRecognitionBinding
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveSignRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && allPermissionsGranted()) {
            startCamera()
        } else {
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Build the preview use case
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            // Build the analyzer use case and attach our custom SignAnalyzer.
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor, SignAnalyzer(this) { word ->
                        // Update UI with the current accumulated word
                        runOnUiThread {
                            binding.signText.text = word
                        }
                    })
                }

            // Choose a camera (using the front camera here)
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .apply {
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    } else {
                        throw IllegalArgumentException("No available cameras on the device")
                    }
                }
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("LiveSignRecognition", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
