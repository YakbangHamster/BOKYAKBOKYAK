package com.example.yakbanghamster.data

import com.google.gson.annotations.SerializedName

data class DuplicateCheckResponse(
    val status: Int,
    val message: String,
    val data: IdentityExistData
)

data class IdentityExistData(
    @SerializedName("isExist") val isExist: Boolean
)

