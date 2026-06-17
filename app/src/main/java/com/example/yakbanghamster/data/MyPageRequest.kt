package com.example.yakbanghamster.data

data class MyPageRequest(
    val name: String,
    val age: Int,
    val sex: Boolean?, // true: 남성, false: 여성
    val disease: List<String> // 상세 정보/질환
)