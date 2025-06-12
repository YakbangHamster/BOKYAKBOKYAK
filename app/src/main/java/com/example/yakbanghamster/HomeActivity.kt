package com.example.yakbanghamster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.adapter.MedicineTodayAdapter
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {
    override val layoutResId: Int
        get() = R.layout.activity_home

    private lateinit var medicineAdapter: MedicineTodayAdapter

    private val medicineDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshTodayMedicines()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pillSideEffectsBtn = findViewById<LinearLayout>(R.id.btn_pill_side_effects)
        val pillSearchBtn = findViewById<LinearLayout>(R.id.btn_pill_search)
        val pillRegisterBtn = findViewById<LinearLayout>(R.id.btn_pill_register)
        val pillReportBtn = findViewById<LinearLayout>(R.id.btn_pill_report)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)

        pillSideEffectsBtn.setOnClickListener {
            startActivity(Intent(this, PillSideEffectsActivity::class.java))
        }

        pillSearchBtn.setOnClickListener {
            startActivity(Intent(this, PillSearchActivity::class.java))
        }

        pillRegisterBtn.setOnClickListener {
            startActivity(Intent(this, PillRegisterActivity::class.java))
        }

        pillReportBtn.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        // 리사이클러뷰 및 어댑터 연결
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_medicine_home)
        medicineAdapter = MedicineTodayAdapter(emptyList()) { item ->
            // 예시: 상세 입력 액티비티로 이동
            val intent = Intent(this, PillDetailActivity::class.java)
            intent.putExtra("medicine_name", item.medicineName)
            medicineDetailLauncher.launch(intent)
        }
        recyclerView.adapter = medicineAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 유저 이름 불러와서 환영 문구 표시
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.userService.getUserName()
                val name = response.data
                val message = "$name 님, 반가워요\n오늘의 컨디션은\n어떤가요?"

                val spannable = SpannableString(message)
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFA655")),
                    0,
                    name.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                welcomeText.text = spannable
            } catch (e: Exception) {
                welcomeText.text = "반가워요\n오늘의 컨디션은\n어떤가요?"
            }
        }

        // 최초 데이터 로딩
        refreshTodayMedicines()
    }

    // 오늘의 약 리스트 갱신 함수
    private fun refreshTodayMedicines() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getTodayMedicines()
                if (response.isSuccessful) {
                    val medicines = response.body()?.data ?: emptyList()
                    medicineAdapter.updateItems(medicines)
                } else {
                    Toast.makeText(this@HomeActivity, "약 리스트 조회 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
