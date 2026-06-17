package com.example.yakbanghamster

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.OnBackPressedCallback
import com.example.yakbanghamster.data.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_mypage

    override val defaultSelectedIndex: Int
        get() = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val profileEditLayout = findViewById<LinearLayout>(R.id.layout_profile_edit)
        profileEditLayout.setOnClickListener {
            startActivity(Intent(this, MyPageUpdateActivity::class.java))
        }

        val passwordLayout = findViewById<LinearLayout>(R.id.layout_password)
        passwordLayout.setOnClickListener {
            val currentId = getCurrentUserIdFromPrefs()
            Log.d("MyPage", "현재 identity: $currentId")
            if (currentId.isNullOrEmpty()) {
                Toast.makeText(this, "로그인 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPasswordDialog(currentId)
        }

        // 로그아웃
        val logoutLayout = findViewById<LinearLayout>(R.id.layout_logout)
        logoutLayout.setOnClickListener {
            performLogout()
        }

        // 계정 삭제
        val deleteAccountText = findViewById<TextView>(R.id.tv_delete_account)
        deleteAccountText.setOnClickListener {
            showDeleteAccountDialog()
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("MyPage", "백버튼 - Home으로 강제 이동")
                val intent = Intent(this@MyPageActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            }
        })

    }

    private fun getCurrentUserIdFromPrefs(): String? {
        val prefs = getSharedPreferences("yakbang_prefs", MODE_PRIVATE)
        return prefs.getString("identity", null)
    }

    private fun clearLoginPrefs() {
        val prefs = getSharedPreferences("yakbang_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun goToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun performLogout() {
        RetrofitInstance.userService.logout()
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    clearLoginPrefs()
                    goToWelcome()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    clearLoginPrefs()
                    goToWelcome()
                }
            })
    }

    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_delete_account, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteAccount)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            RetrofitInstance.userService.deleteAccount()
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            dialog.dismiss()
                            clearLoginPrefs()
                            goToWelcome()
                            Toast.makeText(
                                this@MyPageActivity,
                                "계정이 삭제되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MyPageActivity,
                                "계정 삭제에 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(
                            this@MyPageActivity,
                            "네트워크 오류: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        dialog.show()
    }

    private fun showPasswordDialog(identity: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
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

            val requestBody = mapOf(
                "identity" to identity,
                "password" to newPassword
            )

            RetrofitInstance.userService.changePassword(requestBody)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(
                                this@MyPageActivity,
                                "비밀번호가 변경되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
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
}
