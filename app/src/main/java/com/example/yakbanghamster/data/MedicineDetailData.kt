package com.example.yakbanghamster.data

data class MedicineDetailData(
    val name: String,
    val schedule: List<Boolean>,
    val startDate: String,
    val endDate: String,
    val number: Int,
    val time: List<String>
)
