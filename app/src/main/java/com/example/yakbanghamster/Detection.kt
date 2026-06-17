package com.example.yakbanghamster

data class Detection(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val score: Float,
    val classId: Int
)
