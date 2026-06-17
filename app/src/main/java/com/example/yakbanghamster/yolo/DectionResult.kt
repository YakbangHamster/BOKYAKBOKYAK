package com.example.yakbanghamster.yolo

import android.graphics.RectF

data class DetectionResult(
    val rect: RectF,
    val label: String,
    val confidence: Float
)

