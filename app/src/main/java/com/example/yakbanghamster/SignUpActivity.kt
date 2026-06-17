package com.example.yakbanghamster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.data.DuplicateCheckResponse
import com.example.yakbanghamster.data.LoginRequest
import com.example.yakbanghamster.data.LoginResponse
import com.example.yakbanghamster.data.SignUpRequest
import com.example.yakbanghamster.data.SignUpResponse
import com.example.yakbanghamster.data.RetrofitInstance
import com.example.yakbanghamster.ui.activity.UserDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    // 중복확인 상태 변수
    private var isIdChecked = false
    private var lastCheckedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val loginNow = findViewById<TextView>(R.id.loginNow)
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val duplicateMessage = findViewById<TextView>(R.id.duplicateMessage)

        backButton.setOnClickListener {
            finish()
        }

        loginNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 아이디 입력란이 바뀌면 중복확인 상태 초기화
        editTextId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isIdChecked = false
                lastCheckedId = null
                duplicateMessage.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 중복확인 버튼 클릭 시
        checkDuplicateButton.setOnClickListener {
            val inputId = editTextId.text.toString().trim()
            if (inputId.isEmpty()) {
                duplicateMessage.text = "아이디를 입력해주세요!"
                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                duplicateMessage.visibility = View.VISIBLE
                isIdChecked = false
                lastCheckedId = null
                return@setOnClickListener
            }

            RetrofitInstance.api.checkIdentityDuplicate(inputId)
                .enqueue(object : Callback<DuplicateCheckResponse> {
                    override fun onResponse(
                        call: Call<DuplicateCheckResponse>,
                        response: Response<DuplicateCheckResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.data.isExist) {
                                duplicateMessage.text = "중복되는 아이디가 존재해요!"
                                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                                isIdChecked = false
                                lastCheckedId = null
                            } else {
                                duplicateMessage.text = "사용 가능한 아이디예요!"
                                duplicateMessage.setTextColor(Color.parseColor("#3CCCCA"))
                                isIdChecked = true
                                lastCheckedId = inputId
                            }
                            duplicateMessage.visibility = View.VISIBLE
                        } else {
                            Log.e("DuplicateCheck", "errorBody: ${response.errorBody()?.string()}")
                            Log.e("DuplicateCheck", "raw: ${response.raw()}")
                            duplicateMessage.text = "중복확인 실패: 서버 응답 오류"
                            duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                            duplicateMessage.visibility = View.VISIBLE
                            isIdChecked = false
                            lastCheckedId = null
                        }
                    }

                    override fun onFailure(call: Call<DuplicateCheckResponse>, t: Throwable) {
                        duplicateMessage.text = "네트워크 오류: ${t.message}"
                        duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                        duplicateMessage.visibility = View.VISIBLE
                        isIdChecked = false
                        lastCheckedId = null
                    }
                })
        }

        // 회원가입 버튼 클릭 시
        signUpButton.setOnClickListener {
            val username = editTextId.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isIdChecked || lastCheckedId != username) {
                Toast.makeText(this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordPattern = Regex("^(?=.*[a-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}\$")

            if (!password.matches(passwordPattern)) {
                Toast.makeText(
                    this,
                    "비밀번호는 소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = SignUpRequest(username, email, password)
            RetrofitInstance.api.signUp(request)
                .enqueue(object : Callback<SignUpResponse> {
                    override fun onResponse(
                        call: Call<SignUpResponse>,
                        response: Response<SignUpResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {

                            // 회원가입 성공 후 자동 로그인
                            RetrofitInstance.apiLogin.login(LoginRequest(username, password))
                                .enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(
                                        call: Call<LoginResponse>,
                                        loginResponse: Response<LoginResponse>
                                    ) {
                                        val data = loginResponse.body()?.data
                                        if (loginResponse.isSuccessful && data?.accessToken != null) {
                                            val token = data.accessToken

                                            val prefs = getSharedPreferences("yakbang_prefs", MODE_PRIVATE)
                                            prefs.edit().putString("accessToken", token).apply()
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "회원가입 및 로그인 성공!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // 새 로그인 세션이므로 전역 캐시 초기화
                                            TodayMedicineCache.medicines = null
                                            TodayMedicineCache.imageCache.clear()
                                            AppState.needRefreshTodayMedicines = true

                                            val intent = Intent(
                                                this@SignUpActivity,
                                                UserDetailActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "자동 로그인 실패: ${loginResponse.code()}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<LoginResponse>,
                                        t: Throwable
                                    ) {
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "자동 로그인 네트워크 오류: ${t.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                "회원가입 실패: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                        Toast.makeText(
                            this@SignUpActivity,
                            "네트워크 오류: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}

