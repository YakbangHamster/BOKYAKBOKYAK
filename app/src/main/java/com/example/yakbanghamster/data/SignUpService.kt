package com.example.yakbanghamster.network

import com.example.yakbanghamster.data.SignUpRequest
import com.example.yakbanghamster.data.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpService {
    @POST("users /sign-up")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>
}
