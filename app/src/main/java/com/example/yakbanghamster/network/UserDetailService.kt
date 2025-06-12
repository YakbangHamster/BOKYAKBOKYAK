package com.example.yakbanghamster.network

import com.example.yakbanghamster.viewmodel.UserDetail
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH

interface UserDetailService {
    @PATCH("users/detail")
    fun sendUserDetail(@Body userDetail: UserDetail): Call<Void>
}
