package com.example.yakbanghamster

import android.graphics.*
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ImageUtils {

    private val nv21BufferCache = mutableMapOf<Int, ByteArray>()

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val width = image.width
        val height = image.height
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val totalSize = ySize + uSize + vSize
        val nv21 = nv21BufferCache.getOrPut(totalSize) { ByteArray(totalSize) }

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun bitmapToInputBuffer(bitmap: Bitmap, inputBuffer: ByteBuffer) {
        val width = bitmap.width
        val height = bitmap.height
        inputBuffer.rewind()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val px = bitmap.getPixel(x, y)
                inputBuffer.putFloat(((px shr 16) and 0xFF) / 255f) // R
                inputBuffer.putFloat(((px shr 8) and 0xFF) / 255f)  // G
                inputBuffer.putFloat((px and 0xFF) / 255f)          // B
            }
        }
        inputBuffer.rewind()
    }

    fun scaleBitmap(src: Bitmap, targetSize: Int): Bitmap {
        if (src.width == targetSize && src.height == targetSize) return src
        return Bitmap.createScaledBitmap(src, targetSize, targetSize, true)
    }
}
