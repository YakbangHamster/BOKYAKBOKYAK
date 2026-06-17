package com.example.yakbanghamster

import android.graphics.Color
import android.content.Intent
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.data.DuplicateCheckResponse
import com.example.yakbanghamster.data.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_password)

        val btnFindPassword = findViewById<Button>(R.id.btnFindPassword)
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val duplicateMessage = findViewById<TextView>(R.id.duplicateMessage)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        btnFindPassword.setOnClickListener {
            val inputId = editTextId.text.toString().trim()
            if (inputId.isEmpty()) {
                duplicateMessage.text = "아이디를 입력해 주세요!"
                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                duplicateMessage.visibility = View.VISIBLE
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
                                duplicateMessage.visibility = View.GONE
                                showPasswordDialog(inputId)
                            } else {
                                duplicateMessage.text = "존재하지 않는 아이디입니다."
                                duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                                duplicateMessage.visibility = View.VISIBLE
                            }
                        } else {
                            duplicateMessage.text = "중복확인 실패: 서버 응답 오류"
                            duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                            duplicateMessage.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<DuplicateCheckResponse>, t: Throwable) {
                        duplicateMessage.text = "네트워크 오류: ${t.message}"
                        duplicateMessage.setTextColor(Color.parseColor("#E93B5E"))
                        duplicateMessage.visibility = View.VISIBLE
                    }
                })
        }


        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showPasswordDialog(inputId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialogView.findViewById<android.widget.ImageView>(R.id.btnClose)
        val editNewPassword = dialogView.findViewById<EditText>(R.id.editNewPassword)
        val editConfirmPassword = dialogView.findViewById<EditText>(R.id.editConfirmPassword)
        val btnChangePassword = dialogView.findViewById<Button>(R.id.btnChangePassword)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnChangePassword.setOnClickListener {
            val newPassword = editNewPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                editConfirmPassword.error = "비밀번호를 모두 입력해 주세요"
                return@setOnClickListener
            }
            if (newPassword != confirmPassword) {
                editConfirmPassword.error = "비밀번호가 일치하지 않습니다"
                return@setOnClickListener
            }

            // 여기 수정: PasswordRequest → Map
            val requestBody = mapOf(
                "identity" to inputId,
                "password" to newPassword
            )

            RetrofitInstance.userService.changePassword(requestBody)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            dialog.dismiss()
                            showSuccessDialog()
                        } else {
                            editConfirmPassword.error = "비밀번호 변경 실패: 서버 오류"
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        editConfirmPassword.error = "네트워크 오류: ${t.message}"
                    }
                })
        }

        dialog.show()
    }


    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setMessage("비밀번호가 성공적으로 변경되었습니다!\n로그인 화면으로 이동합니다.")
            .setCancelable(false)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .show()
    }


}

