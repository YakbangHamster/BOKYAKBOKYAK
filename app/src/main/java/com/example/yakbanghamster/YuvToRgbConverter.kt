package com.example.yakbanghamster

import android.content.Context
import android.graphics.*
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.graphics.Rect
import java.io.ByteArrayOutputStream

class YuvToRgbConverter(context: Context) {
    fun yuvToRgb(yuvData: ByteArray, outBitmap: Bitmap) {
        val yuv = YuvImage(yuvData, ImageFormat.NV21, outBitmap.width, outBitmap.height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, outBitmap.width, outBitmap.height), 100, out)
        val jpegBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        outBitmap.eraseColor(0)
        val canvas = Canvas(outBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}
