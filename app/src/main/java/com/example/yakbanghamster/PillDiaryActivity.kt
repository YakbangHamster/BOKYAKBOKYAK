package com.example.yakbanghamster

import MedicineSimpleAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch

class PillDiaryActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_diary

    private lateinit var adapter: MedicineSimpleAdapter

    // ActivityResultLauncher 선언
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 상세정보 입력 후 돌아왔을 때 약 리스트 갱신
            refreshMedicineList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.medicineRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MedicineSimpleAdapter(emptyList()) { medicine ->
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.medicineService.getMedicineDetail(medicine.medicineName)
                    if (response.isSuccessful && response.body()?.data != null) {
                        val intent = Intent(this@PillDiaryActivity, PillEditDetailActivity::class.java)
                        intent.putExtra("medicine_name", medicine.medicineName)
                        startActivity(intent)
                    } else {
                        // 상세정보 없으면 detailLauncher로 PillDetailActivity 실행
                        val intent = Intent(this@PillDiaryActivity, PillDetailActivity::class.java)
                        intent.putExtra("medicine_name", medicine.medicineName)
                        detailLauncher.launch(intent)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PillDiaryActivity, "상세 조회 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = adapter

        // 최초 데이터 로딩
        refreshMedicineList()
    }

    // 약 리스트 갱신 함수
    private fun refreshMedicineList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getMedicines(0)
                Log.d("PillDiaryActivity", "받은 데이터: ${response.data}")
                adapter.updateItems(response.data)
            } catch (e: Exception) {
                Toast.makeText(
                    this@PillDiaryActivity,
                    "데이터 불러오기 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}



