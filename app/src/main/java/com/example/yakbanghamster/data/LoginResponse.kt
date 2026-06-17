package com.example.yakbanghamster.data

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: TokenData?
)

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val identity: String
)

