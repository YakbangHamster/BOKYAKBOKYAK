package com.example.yakbanghamster

import MedicineAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.data.Medicine
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch

class PillListActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_list

    private lateinit var adapter: MedicineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backbutton = findViewById<ImageView>(R.id.btn_back)
        backbutton.setOnClickListener {
            startActivity(Intent(this, PillSearchActivity::class.java))
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.medicineRecyclerView)
        val loadingLayout = findViewById<View>(R.id.loadingLayout)
        val contentLayout = findViewById<View>(R.id.contentLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MedicineAdapter(
            items = emptyList(),
            onItemClick = { medicine ->
                val intent = Intent(this, PillInfoActivity::class.java)
                intent.putExtra("medicineName", medicine.name)
                intent.putExtra("medicineSerial", medicine.serial)
                intent.putExtra("medicineEfficacy", medicine.efficacy)
                intent.putExtra("medicineHowToTake", medicine.howToTake)
                intent.putExtra("medicineImage", medicine.image)
                startActivity(intent)
            },
            onFooterClick = {
                Toast.makeText(this@PillListActivity, "더보기 클릭!", Toast.LENGTH_SHORT).show()
            },
            showFooter = false,
            footerLayoutRes = R.layout.item_footer_more
        )
        recyclerView.adapter = adapter

        val keyword = intent.getStringExtra("search_keyword") ?: ""

        if (keyword.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    loadingLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.GONE

                    val response = RetrofitInstance.medicineService.searchMedicines(
                        medicineName = keyword,
                        page = 0
                    )
                    val medicines = response.data.map {
                        Medicine(
                            name = it.name,
                            image = it.image,
                            efficacy = it.efficacy,
                            howToTake = it.howToTake,
                            serial = it.serial
                        )
                    }

                    adapter = MedicineAdapter(
                        items = medicines,
                        onItemClick = { medicine ->
                            val intent = Intent(this@PillListActivity, PillInfoActivity::class.java)
                            intent.putExtra("medicineName", medicine.name)
                            intent.putExtra("medicineSerial", medicine.serial)
                            intent.putExtra("medicineEfficacy", medicine.efficacy)
                            intent.putExtra("medicineHowToTake", medicine.howToTake)
                            intent.putExtra("medicineImage", medicine.image)
                            startActivity(intent)
                        },
                        onFooterClick = {
                            Toast.makeText(this@PillListActivity, "더보기 클릭!", Toast.LENGTH_SHORT).show()
                        },
                        showFooter = true,
                        footerLayoutRes = R.layout.item_footer_more
                    )
                    recyclerView.adapter = adapter
                } catch (e: Exception) {
                    android.util.Log.e("PillListActivity", "검색 실패", e)
                    if (e is retrofit2.HttpException) {
                        val code = e.code()
                        val errorBody = e.response()?.errorBody()?.string()
                        android.util.Log.e("PillListActivity", "HTTP 오류 코드: $code")
                        android.util.Log.e("PillListActivity", "서버 에러 바디: $errorBody")
                    }
                    Toast.makeText(
                        this@PillListActivity,
                        "검색 결과를 불러오지 못했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {

                    loadingLayout.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                }
            }
        }
    }
}

