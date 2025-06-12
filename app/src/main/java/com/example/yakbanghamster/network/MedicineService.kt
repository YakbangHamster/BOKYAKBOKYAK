package com.example.yakbanghamster.network

import MedicineReportResponse
import com.example.yakbanghamster.data.MedicineDetailResponse
import com.example.yakbanghamster.data.MedicineListResponse
import com.example.yakbanghamster.data.MedicineSearchResponse
import com.example.yakbanghamster.data.MedicineTodayResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MedicineService {
    @GET("/medicines/search")
    suspend fun searchMedicines(
        @Query("medicineName") medicineName: String,
        @Query("page") page: Int = 0
    ): MedicineSearchResponse

    @POST("/medicines/ocr")
    suspend fun ocrMedicines(
        @Body body: RequestBody
    ): ResponseBody

    @POST("/medicines/detail")
    suspend fun postMedicineDetail(
        @Body body: RequestBody
    ): ResponseBody

    @GET("/medicines/detail")
    suspend fun getMedicineDetail(@Query("medicineName") medicineName: String): Response<MedicineDetailResponse>

    @PATCH("/medicines/detail")
    suspend fun patchMedicineDetail(
        @Body body: RequestBody
    ): Response<ResponseBody>

    @GET("/medicines")
    suspend fun getMedicines(
        @Query("page") page: Int = 0
    ): MedicineListResponse

    @GET("/medicines/side-effects/{name}")
    suspend fun getSideEffect(@Path("name") medicineName: String): String

    @GET("/medicines/today")
    suspend fun getTodayMedicines(): Response<MedicineTodayResponse>

    @GET("medicines/report")
    suspend fun getMedicineReport(): Response<MedicineReportResponse>

}

