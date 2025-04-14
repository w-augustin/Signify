package com.example.signifybasic.signrecognition

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SignAnalyzer(
    context: Context,
    private val onSignDetected: (String) -> Unit  // Callback to update the UI with the accumulated word
) : ImageAnalysis.Analyzer {

    private val interpreter: Interpreter
    private val handLandmarkerHelper: HandLandmarkerHelper
    private var lastLetter: String = ""
    private var staticGestureCount: Int = 0
    private val wordBuilder = StringBuilder()

    companion object {
        private const val STATIC_THRESHOLD = 6  // Frames needed for a stable prediction.
    }

    init {
        // Initialize the helper in LIVE_STREAM mode.
        handLandmarkerHelper = HandLandmarkerHelper(
            context = context,
            runningMode = com.google.mediapipe.tasks.vision.core.RunningMode.LIVE_STREAM,
            handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {
                    Log.e("SignAnalyzer", "HandLandmarker error: $error, code: $errorCode")
                }

                // Callback fired asynchronously when detection is complete.
                override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                    // If any hands were detected…
                    if (resultBundle.results.isNotEmpty()) {
                        // For simplicity, take the first detected hand.
                        val landmarks = resultBundle.results[0].landmarks()[0]
                        val predictedLetter = predictSign(landmarks)
                        // Temporal smoothing to avoid flickering predictions:
                        if (predictedLetter == lastLetter) {
                            staticGestureCount++
                        } else {
                            lastLetter = predictedLetter
                            staticGestureCount = 0
                        }
                        if (staticGestureCount > STATIC_THRESHOLD) {
                            wordBuilder.append(predictedLetter)
                            staticGestureCount = 0
                        }
                        // Send the accumulated word to your UI.
                        onSignDetected(wordBuilder.toString())
                    }
                }
            }
        )
        // Load your gesture classification TFLite model.
        val model = loadModelFile(context, "gesture_model.tflite")
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun analyze(image: ImageProxy) {
        try {
            if (image.format != ImageFormat.YUV_420_888) {
                image.close()
                return
            }
            // NOTE: Although earlier instructions describe converting the Bitmap to an MPImage,
            // your helper's detectLiveStream expects an ImageProxy. So we pass the ImageProxy directly.
            handLandmarkerHelper.detectLiveStream(image, isFrontCamera = true)
        } catch (e: Exception) {
            Log.e("SignAnalyzer", "Error in analyze: ", e)
        } finally {
            image.close()
        }
    }

    private fun predictSign(landmarks: List<NormalizedLandmark>): String {
        val wrist = landmarks[0]
        // Create a feature vector using the relative positions of landmarks from the wrist.
        val distances = FloatArray(landmarks.size * 2)
        for ((i, landmark) in landmarks.withIndex()) {
            distances[i * 2] = landmark.x() - wrist.x()
            distances[i * 2 + 1] = landmark.y() - wrist.y()
        }
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, distances.size), org.tensorflow.lite.DataType.FLOAT32)
        inputBuffer.loadArray(distances)
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 24), org.tensorflow.lite.DataType.FLOAT32)
        interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())
        val output = outputBuffer.floatArray
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return ('A' + maxIndex).toString()
    }
}
