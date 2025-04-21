package com.example.signifybasic.features.tabs.playground.liverecognition

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import android.content.res.AssetFileDescriptor
import kotlin.math.exp

class HandTracker(context: Context) {

    private val palmInterpreter: Interpreter = Interpreter(loadModelFile(context, "palm_detection.tflite"))
    private val landmarkInterpreter: Interpreter = Interpreter(loadModelFile(context, "hand_landmark_3d.tflite"))
    private val anchors: Array<FloatArray> = loadAnchors(context)

    companion object {
        private const val INPUT_IMAGE_SIZE = 256
        private const val PALM_SCORE_THRESHOLD = 0.7f
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadAnchors(context: Context): Array<FloatArray> {
        val inputStream = context.assets.open("anchors.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val anchorsList = mutableListOf<FloatArray>()
        reader.forEachLine {
            val row = it.split(",").map(String::toFloat).toFloatArray()
            anchorsList.add(row)
        }
        return anchorsList.toTypedArray()
    }

    private fun normalizeImage(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(1 * INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)
        val intValues = IntArray(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE)
        resized.getPixels(intValues, 0, INPUT_IMAGE_SIZE, 0, 0, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE)
        var pixel = 0
        for (i in 0 until INPUT_IMAGE_SIZE) {
            for (j in 0 until INPUT_IMAGE_SIZE) {
                val value = intValues[pixel++]
                imgData.putFloat(((value shr 16 and 0xFF) / 255.0f - 0.5f) * 2.0f)
                imgData.putFloat(((value shr 8 and 0xFF) / 255.0f - 0.5f) * 2.0f)
                imgData.putFloat(((value and 0xFF) / 255.0f - 0.5f) * 2.0f)
            }
        }
        return imgData
    }

    private fun sigmoid(x: Float): Float = 1 / (1 + exp(-x))

    fun detect(bitmap: Bitmap): FloatArray? {
        val input = normalizeImage(bitmap)
        val outputReg = Array(1) { Array(2944) { FloatArray(18) } } // adjust based on model shape
        val outputClf = Array(1) { Array(2944) { FloatArray(1) } }

        palmInterpreter.runForMultipleInputsOutputs(arrayOf(input), mapOf(
            0 to outputReg,
            1 to outputClf
        ))

        val reg = outputReg[0]
        val clf = outputClf[0].map { sigmoid(it[0]) }

        val selected = clf.withIndex().filter { it.value > PALM_SCORE_THRESHOLD }
        if (selected.isEmpty()) return null

        val topIdx = selected.maxByOrNull { it.value }!!.index
        val keypoints = reg[topIdx].sliceArray(4 until 18)
        return keypoints
    }

    fun getLandmarks(handBitmap: Bitmap): FloatArray {
        val input = normalizeImage(handBitmap)
        val output = Array(1) { FloatArray(63) }  // ✅ EXPECT 63 VALUES
        landmarkInterpreter.run(input, output)
        return output[0]
    }


    fun close() {
        palmInterpreter.close()
        landmarkInterpreter.close()
    }
}