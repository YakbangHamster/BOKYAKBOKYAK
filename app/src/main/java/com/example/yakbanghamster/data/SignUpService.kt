package com.example.yakbanghamster.network

import com.example.yakbanghamster.data.DuplicateCheckResponse
import com.example.yakbanghamster.data.SignUpRequest
import com.example.yakbanghamster.data.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SignUpService {
    @POST("users/sign-up")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>

    @GET("users/identity/check")
    fun checkIdentityDuplicate(@Query("identity") identity: String): Call<DuplicateCheckResponse>

}
