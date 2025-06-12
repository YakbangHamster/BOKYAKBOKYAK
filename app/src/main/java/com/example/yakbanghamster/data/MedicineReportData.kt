package com.example.yakbanghamster.data

data class MedicineReportData(
    val username: String,
    val startDate: String,
    val endDate: String,
    val schedule: List<Int>,
    val medication: List<MedicationItem>,
    val compliance: Double
)
