package com.example.yakbanghamster.data

data class MedicineTodayData(
    val medicineName: String,
    val timeList: List<String>,
    val image: String? = null
)
