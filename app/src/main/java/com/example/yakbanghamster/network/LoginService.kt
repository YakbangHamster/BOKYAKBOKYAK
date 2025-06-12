package com.example.yakbanghamster.network

import com.example.yakbanghamster.data.LoginRequest
import com.example.yakbanghamster.data.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("users/sign-in")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
