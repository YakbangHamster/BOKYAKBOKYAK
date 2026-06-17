package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.data.LoginRequest
import com.example.yakbanghamster.data.LoginResponse
import com.example.yakbanghamster.data.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val loginService = RetrofitInstance.apiLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        currentFocus?.clearFocus()

        setContentView(R.layout.activity_login)

        val identityEditText = findViewById<EditText>(R.id.editTextId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val findPasswordButton = findViewById<Button>(R.id.findPasswordButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val registerNow = findViewById<TextView>(R.id.registerNow)
        val loginErrorText = findViewById<TextView>(R.id.tvLoginError)

        fun showError(message: String) {
            loginErrorText.text = message
            loginErrorText.visibility = TextView.VISIBLE
        }

        fun clearError() {
            loginErrorText.text = ""
            loginErrorText.visibility = TextView.GONE
        }

        loginButton.setOnClickListener {
            Log.d("LoginDebug", "로그인 버튼 클릭됨!")
            clearError()

            val identity = identityEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (identity.isEmpty() || password.isEmpty()) {
                showError("아이디와 비밀번호를 입력해 주세요.")
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(identity, password)

            loginService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val tokenData = body?.data
                        if (tokenData != null && tokenData.accessToken.isNotEmpty()) {
                            val accessToken = tokenData.accessToken
                            val refreshToken = tokenData.refreshToken
                            val identity = tokenData.identity

                            val prefs = getSharedPreferences("yakbang_prefs", MODE_PRIVATE)
                            prefs.edit()
                                .putString("accessToken", accessToken)
                                .putString("refreshToken", refreshToken)
                                .putString("identity", identity)
                                .apply()

                            // 새 로그인 세션이므로 전역 캐시 초기화
                            TodayMedicineCache.medicines = null
                            TodayMedicineCache.imageCache.clear()
                            AppState.needRefreshTodayMedicines = true

                            Log.d("LoginDebug", "로그인 성공 - HomeActivity로 이동")
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    HomeActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            showError(body?.message ?: "아이디 또는 비밀번호가 올바르지 않습니다.")
                        }
                    } else {
                        showError("아이디 또는 비밀번호가 올바르지 않습니다.")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginDebug", "로그인 실패: ${t.message}")
                    showError("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
                }
            })
        }

        findPasswordButton.setOnClickListener {
            Log.d("LoginDebug", "비밀번호 찾기 버튼 클릭!")
            val intent = Intent(this, FindPasswordActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            Log.d("LoginDebug", "뒤로가기 버튼 클릭!")
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        registerNow.setOnClickListener {
            Log.d("Register", "회원가입 버튼 CLICKED!")
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
