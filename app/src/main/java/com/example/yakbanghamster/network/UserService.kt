package com.example.yakbanghamster.network

import com.example.yakbanghamster.PasswordRequest
import com.example.yakbanghamster.data.UserNameResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserService {
    @PATCH("users/password")
    fun changePassword(@Body request: PasswordRequest): Call<Void>

    @GET("/users/name")
    suspend fun getUserName(): UserNameResponse
}


