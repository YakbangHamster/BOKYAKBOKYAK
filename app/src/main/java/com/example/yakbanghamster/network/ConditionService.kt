package com.example.yakbanghamster.network

import com.example.yakbanghamster.data.ConditionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ConditionService {
    @POST("conditions")
    suspend fun postCondition(@Body body: Map<String, String>): Response<Unit>

    @PUT("conditions")
    suspend fun updateCondition(@Body body: Map<String, String>): Response<Unit>

    @GET("conditions")
    suspend fun getCondition(@Query("date") date: String): Response<ConditionResponse>

}



