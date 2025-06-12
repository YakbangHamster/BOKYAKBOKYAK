package com.example.yakbanghamster.data

data class MedicineTodayResponse(
    val status: Int,
    val message: String,
    val data: List<MedicineTodayData>
)