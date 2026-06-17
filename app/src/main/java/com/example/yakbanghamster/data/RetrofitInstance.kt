package com.example.yakbanghamster.data

import android.content.Context
import com.example.yakbanghamster.App
import com.example.yakbanghamster.network.ConditionService
import com.example.yakbanghamster.network.LoginService
import com.example.yakbanghamster.network.SignUpService
import com.example.yakbanghamster.network.UserDetailService
import com.example.yakbanghamster.network.UserService
import com.example.yakbanghamster.network.MedicineService
import com.example.yakbanghamster.network.MyPageService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private fun getToken(): String? {

        return App.context.getSharedPreferences("yakbang_prefs", Context.MODE_PRIVATE)
            .getString("accessToken", null)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath

            // 토큰이 필요 없는 엔드포인트 목록(중복확인, 회원가입, 로그인(처음 토큰 발급받을 때))
            val noAuthPaths = setOf(
                "/users/identity/check",
                "/users/sign-up",
                "/users/sign-in"
            )

            if (path in noAuthPaths) {
                return@addInterceptor chain.proceed(request)
            }
            val token = getToken()
            val requestBuilder = chain.request().newBuilder()
            if (token != null) {
                android.util.Log.d("RetrofitInstance", "토큰: Bearer $token")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                android.util.Log.w("RetrofitInstance", "토큰이 없습니다!")
            }
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(60, TimeUnit.SECONDS) // 서버 연결 최대 60초
        .readTimeout(60, TimeUnit.SECONDS)    // 서버 응답 대기 최대 60초
        .writeTimeout(60, TimeUnit.SECONDS)   // 서버로 데이터 전송 최대 60초
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.19.24.127:8080/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiLogin: LoginService by lazy {
        retrofit.create(LoginService::class.java)
    }

    val api: SignUpService by lazy {
        retrofit.create(SignUpService::class.java)
    }

    val userDetailService by lazy {
        retrofit.create(UserDetailService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val medicineService: MedicineService by lazy {
        retrofit.create(MedicineService::class.java)
    }

    val conditionService: ConditionService by lazy {
        retrofit.create(ConditionService::class.java)
    }

    val myPageService: MyPageService by lazy {
        retrofit.create(MyPageService::class.java)
    }

}

