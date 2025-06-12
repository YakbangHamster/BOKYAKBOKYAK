package com.example.yakbanghamster.data

data class MedicineSearchResponse(
    val status: Int,
    val message: String,
    val data: List<MedicineDto>
)

data class MedicineDto(
    val serial: String,
    val name: String,
    val image: String,
    val efficacy: String,
    val howToTake: String
)
