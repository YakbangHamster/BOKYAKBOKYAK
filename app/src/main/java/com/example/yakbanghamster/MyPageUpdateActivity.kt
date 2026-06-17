package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.data.RetrofitInstance
import com.example.yakbanghamster.data.MyPageRequest
import com.example.yakbanghamster.BaseActivity
import com.example.yakbanghamster.data.MyPageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageUpdateActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_mypage_update

    // true: 남성, false: 여성
    private var selectedSex: Boolean? = null

    private lateinit var editName: EditText
    private lateinit var editAge: EditText
    private lateinit var editDisease: EditText
    private lateinit var femaleOption: FrameLayout
    private lateinit var maleOption: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        femaleOption = findViewById(R.id.femaleOption)
        maleOption = findViewById(R.id.maleOption)
        val btnEdit = findViewById<Button>(R.id.btn_edit)

        editName = findViewById(R.id.editName)
        editAge = findViewById(R.id.editAge)
        editDisease = findViewById(R.id.editDisease)

        // 처음 들어올 때 서버에서 현재 정보 조회해서 채우기
        loadMyPageData()

        // 성별 선택 클릭 리스너
        femaleOption.setOnClickListener {
            setSex(false)   // 여성 선택
        }

        maleOption.setOnClickListener {
            setSex(true)    // 남성 선택
        }

        // 수정 완료 버튼
        btnEdit.setOnClickListener {
            val name = editName.text.toString().trim()
            val age = editAge.text.toString().trim().toIntOrNull()
            val diseaseInput = editDisease.text.toString().trim()

            if (name.isEmpty() || age == null || selectedSex == null) {
                Toast.makeText(this, "이름, 나이, 성별을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // "고혈압, 당뇨, 위염" → ["고혈압","당뇨","위염"]
            val diseaseList: List<String> =
                if (diseaseInput.isNotEmpty()) {
                    diseaseInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

            sendMyPageUpdate(
                name = name,
                age = age,
                sex = selectedSex,
                diseaseList = diseaseList
            )
        }
    }

    // 성별 UI/값을 한 번에 갱신 (active = 선택, circle = 비선택)
    private fun setSex(isMale: Boolean) {
        selectedSex = isMale
        if (isMale) {
            // 남성 선택
            maleOption.setBackgroundResource(R.drawable.gender_circle_active)
            femaleOption.setBackgroundResource(R.drawable.gender_circle)
        } else {
            // 여성 선택
            femaleOption.setBackgroundResource(R.drawable.gender_circle_active)
            maleOption.setBackgroundResource(R.drawable.gender_circle)
        }
    }

    // 마이페이지 GET으로 현재 데이터 가져오기
    private fun loadMyPageData() {
        val call = RetrofitInstance.myPageService.getMyPage()
        call.enqueue(object : Callback<MyPageResponse> {
            override fun onResponse(
                call: Call<MyPageResponse>,
                response: Response<MyPageResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data

                    if (data != null) {
                        // 이름, 나이 세팅
                        editName.setText(data.name ?: "")
                        editAge.setText(data.age?.toString() ?: "")

                        // 질환 리스트 → "고혈압, 당뇨" 문자열로 합쳐서 EditText에 넣기
                        val diseaseText = data.disease?.joinToString(", ") ?: ""
                        editDisease.setText(diseaseText)

                        // 성별에 따라 선택 표시
                        when (data.sex) {
                            true -> setSex(true)    // 남성
                            false -> setSex(false)  // 여성
                            else -> {
                                // 아무것도 선택 안 된 상태 (selectedSex=null 유지)
                                femaleOption.setBackgroundResource(R.drawable.gender_circle)
                                maleOption.setBackgroundResource(R.drawable.gender_circle)
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MyPageUpdateActivity,
                        "마이페이지 조회 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MyPageResponse>, t: Throwable) {
                Toast.makeText(
                    this@MyPageUpdateActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // PATCH로 수정 요청 보내기
    private fun sendMyPageUpdate(
        name: String,
        age: Int,
        sex: Boolean?,
        diseaseList: List<String>
    ) {
        val body = MyPageRequest(
            name = name,
            age = age,
            sex = sex,
            disease = diseaseList
        )

        val call = RetrofitInstance.myPageService.updateMyPage(body)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MyPageUpdateActivity,
                        "회원정보가 수정되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@MyPageUpdateActivity, MyPageActivity::class.java)
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@MyPageUpdateActivity,
                        "서버 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@MyPageUpdateActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }



}