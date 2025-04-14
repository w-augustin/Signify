import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Extension function to convert an ImageProxy (YUV_420_888) to a Bitmap.
 */
fun ImageProxy.toBitmap(): Bitmap? {
    // Make sure the image is in the expected format.
    if (format != ImageFormat.YUV_420_888) {
        return null
    }

    // Get the three image planes.
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    // Compute the total size of the byte array.
    val ySize = yPlane.buffer.remaining()
    val uSize = uPlane.buffer.remaining()
    val vSize = vPlane.buffer.remaining()
    val nv21ByteArray = ByteArray(ySize + uSize + vSize)

    // Copy the Y plane.
    yPlane.buffer.get(nv21ByteArray, 0, ySize)

    // The U and V planes might have different row strides or pixel strides depending on the device.
    // A simple implementation is to assume they are contiguous and interleaved as NV21 format.
    // NV21 expects: Y plane, then interleaved V and U.
    // Copy V then U:
    vPlane.buffer.get(nv21ByteArray, ySize, vSize)
    uPlane.buffer.get(nv21ByteArray, ySize + vSize, uSize)

    // Create a YuvImage from NV21 byte array
    val yuvImage = YuvImage(
        nv21ByteArray,
        ImageFormat.NV21,
        width,
        height,
        null
    )
    val out = ByteArrayOutputStream()
    // Compress the YuvImage to JPEG with 100% quality.
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()

    // Convert the JPEG byte array to a Bitmap.
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
