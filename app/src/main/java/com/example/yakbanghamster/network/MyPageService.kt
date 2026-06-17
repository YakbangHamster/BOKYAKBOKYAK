package com.example.yakbanghamster.network

import com.example.yakbanghamster.data.MyPageRequest
import com.example.yakbanghamster.data.MyPageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MyPageService {

    @GET("/users/my-page")
    fun getMyPage(): Call<MyPageResponse>

    @PATCH("/users/my-page")
    fun updateMyPage(@Body body: MyPageRequest): Call<Void>
}