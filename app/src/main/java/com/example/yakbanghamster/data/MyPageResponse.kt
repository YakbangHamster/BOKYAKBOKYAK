package com.example.yakbanghamster.data

data class MyPageResponse(
    val status: Int,
    val message: String,
    val data: MyPageData?
)