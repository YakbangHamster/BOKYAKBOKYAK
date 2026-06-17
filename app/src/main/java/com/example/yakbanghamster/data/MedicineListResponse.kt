package com.example.yakbanghamster.data

data class MedicineListResponse(
    val status: Int,
    val message: String,
    val data: List<MedicineSimple>
)