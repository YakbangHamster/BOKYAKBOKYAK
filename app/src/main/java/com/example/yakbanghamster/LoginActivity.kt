package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
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
        setContentView(R.layout.activity_login)

        val identityEditText = findViewById<EditText>(R.id.editTextId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val findPasswordButton = findViewById<Button>(R.id.findPasswordButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val registerNow = findViewById<TextView>(R.id.registerNow)

        loginButton.setOnClickListener {
            val identity = identityEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (identity.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(identity, password)

            loginService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.data != null && body.data.accessToken.isNotEmpty()) {

                            val prefs = getSharedPreferences("yakbang_prefs", MODE_PRIVATE)
                            prefs.edit().putString("accessToken", body.data.accessToken).apply()

                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패: ${body?.message ?: response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        findPasswordButton.setOnClickListener {
            startActivity(Intent(this, FindPasswordActivity::class.java))
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        registerNow.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
