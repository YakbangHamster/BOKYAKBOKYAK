package com.example.yakbanghamster.data

data class DuplicateCheckResponse(
    val status: Int,
    val message: String,
    val data: IdentityExistData
)

data class IdentityExistData(
    val isExist: Boolean
)

