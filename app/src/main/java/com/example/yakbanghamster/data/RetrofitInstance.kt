package com.example.yakbanghamster.data

import com.example.yakbanghamster.network.SignUpService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: SignUpService by lazy {
        Retrofit.Builder()
            .baseUrl("http://your-api-url.com") // 백엔드 주소로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SignUpService::class.java)
    }
}
