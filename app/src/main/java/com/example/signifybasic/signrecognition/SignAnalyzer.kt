package com.example.signifybasic.signrecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.File
import android.content.res.AssetFileDescriptor
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SignAnalyzer(
    context: Context,
    private val onSignDetected: (String) -> Unit  // Callback to update the UI with the accumulated word
) : ImageAnalysis.Analyzer {

    private val gestureInterpreter: Interpreter
    private val handTracker: HandTracker = HandTracker(context)
    private var lastLetter: String = ""
    private var staticGestureCount: Int = 0
    private val wordBuilder = StringBuilder()

    val mean = floatArrayOf(
        82.374864f, 78.174733f, 118.896166f, 140.090894f, 103.462561f,
        31.250618f, 48.557068f, 50.368350f, 65.145337f
    )

    val scale = floatArrayOf(
        43.073782f, 44.222657f, 53.478828f, 43.316311f, 24.972539f,
        30.194658f, 46.130457f, 35.866967f, 41.414944f
    )

    val labelMap = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "K", "L", "M", "N", "O", "P", "Q", "R",
        "S", "T", "U", "V", "W", "X", "Y"
    )

    companion object {
        private const val STATIC_THRESHOLD = 6  // Frames needed for a stable prediction.
    }

    init {
        // Load your gesture classification TFLite model.
        val model = loadModelFile(context, "gesture_model.tflite")
        gestureInterpreter = Interpreter(model)
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

            val bitmap = imageProxyToBitmap(image)
            val keypoints = handTracker.detect(bitmap)

            if (keypoints != null) {
                val landmarks = handTracker.getLandmarks(bitmap)
                val predictedLetter = predictSign(landmarks)
                Log.d("SignAnalyzer", "Predicted letter: $predictedLetter")

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

                val topDisplay = predictSign(landmarks)
                onSignDetected(topDisplay)
            }
        } catch (e: Exception) {
            Log.e("SignAnalyzer", "Error in analyze: ", e)
        } finally {
            image.close()
        }
    }

    private fun extract9FeaturesFromLandmarks(landmarks: FloatArray): FloatArray {
        fun dist(a: Int, b: Int): Float {
            val ax = landmarks[a * 3]
            val ay = landmarks[a * 3 + 1]
            val bx = landmarks[b * 3]
            val by = landmarks[b * 3 + 1]
            return kotlin.math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by))
        }

        return floatArrayOf(
            dist(20, 0),  // dist_20_0
            dist(16, 0),  // dist_16_0
            dist(12, 0),  // dist_12_0
            dist(8, 0),   // dist_8_0
            dist(4, 0),   // dist_4_0
            dist(20, 16), // dist_20_16
            dist(16, 12), // dist_16_12
            dist(12, 8),  // dist_12_8
            dist(8, 4)    // dist_8_4
        )
    }

    private fun predictSign(landmarks: FloatArray): String {
        if (landmarks.size < 63) {
            Log.w("SignAnalyzer", "Expected 21 landmarks (x, y, z) but got ${landmarks.size / 3}")
            return ""
        }

        val rawFeatures = extract9FeaturesFromLandmarks(landmarks)
        val normalized = rawFeatures.mapIndexed { i, v -> (v - mean[i]) / scale[i] }.toFloatArray()

        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 9), org.tensorflow.lite.DataType.FLOAT32)
        inputBuffer.loadArray(normalized)

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 24), org.tensorflow.lite.DataType.FLOAT32)
        gestureInterpreter.run(inputBuffer.buffer.rewind(), outputBuffer.buffer.rewind())

        val output = outputBuffer.floatArray

        val topN = output.mapIndexed { i, prob -> i to prob }
            .sortedByDescending { it.second }
            .take(3)
            .map { (i, prob) -> "${labelMap.getOrElse(i) { "?" }}: ${String.format("%.2f", prob * 100)}%" }

        return topN.joinToString("\n")
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val yuvByteArray = out.toByteArray()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(yuvByteArray, 0, yuvByteArray.size)

        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f) // mirror for front camera
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}