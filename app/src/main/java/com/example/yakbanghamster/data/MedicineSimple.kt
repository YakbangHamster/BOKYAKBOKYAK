package com.example.yakbanghamster.data

data class MedicineSimple(
    val medicineName: String,
    val date: String?,
    val startDate: String?,
    val endDate: String?,
    val image: String? = null
)
