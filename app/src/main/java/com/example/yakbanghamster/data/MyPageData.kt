package com.example.yakbanghamster.data

data class MyPageData(
    val name: String?,
    val email: String?,
    val age: Int?,
    val sex: Boolean?, // true: 남성, false: 여성
    val height: Double?,
    val weight: Double?,
    val disease: List<String>?
)