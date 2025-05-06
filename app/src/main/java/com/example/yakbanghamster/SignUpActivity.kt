package com.example.yakbanghamster

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.data.SignUpRequest
import com.example.yakbanghamster.data.SignUpResponse
import com.example.yakbanghamster.data.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val duplicateMessage = findViewById<TextView>(R.id.duplicateMessage)

        backButton.setOnClickListener {
            finish()
        }

        checkDuplicateButton.setOnClickListener {
            val inputId = editTextId.text.toString().trim()
            if (inputId.isEmpty()) {
                duplicateMessage.text = "아이디를 입력해주세요!"
                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
            } else if (inputId == "hamster123") {
                duplicateMessage.text = "중복되는 아이디가 존재해요!"
                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
            } else {
                duplicateMessage.text = "사용 가능한 아이디예요!"
                duplicateMessage.setTextColor(Color.parseColor("#3CCCCA"))
            }
            duplicateMessage.visibility = View.VISIBLE
        }

        signUpButton.setOnClickListener {
            val username = editTextId.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = SignUpRequest(username, email, password)
            RetrofitInstance.api.signUp(request).enqueue(object : Callback<SignUpResponse> {
                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@SignUpActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
