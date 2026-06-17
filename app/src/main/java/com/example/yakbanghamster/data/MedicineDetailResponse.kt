package com.example.yakbanghamster.data

data class MedicineDetailResponse(
    val status: Int,
    val message: String,
    val data: MedicineDetailData?
)