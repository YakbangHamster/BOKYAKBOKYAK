package com.example.yakbanghamster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.adapter.MedicineTodayAdapter
import com.example.yakbanghamster.data.MedicineSimple
import com.example.yakbanghamster.data.MedicineTodayData
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_home

    private lateinit var medicineAdapter: MedicineTodayAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout

    private val medicineDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshTodayMedicines(forceRefresh = true)
            preloadAllMedicineImages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pillSideEffectsBtn = findViewById<LinearLayout>(R.id.btn_pill_side_effects)
        val pillSearchBtn = findViewById<LinearLayout>(R.id.btn_pill_search)
        val pillRegisterBtn = findViewById<LinearLayout>(R.id.btn_pill_register)
        val pillReportBtn = findViewById<LinearLayout>(R.id.btn_pill_report)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val conditionRecordContainer = findViewById<LinearLayout>(R.id.textUnderlineContainer)

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

        conditionRecordContainer.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        recyclerView = findViewById(R.id.recycler_medicine_home)
        emptyView = findViewById(R.id.empty_medicine_view)

        medicineAdapter = MedicineTodayAdapter(emptyList()) { item ->
        }.apply {
            setOnArrowClickListener { item ->
                val calendarIntent = Intent(this@HomeActivity, CalendarActivity::class.java)
                calendarIntent.putExtra("medicine_name", item.medicineName)
                startActivity(calendarIntent)
            }
        }

        recyclerView.adapter = medicineAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 유저 이름 불러와서 환영 문구 표시
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.userService.getUserName()
                val name = response.data
                val message = "$name 님, 반가워요\n오늘의 컨디션은\n어떤가요?"

                val spannable = SpannableString(message).apply {
                    setSpan(
                        ForegroundColorSpan(Color.parseColor("#FFA655")),
                        0,
                        name.length,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                welcomeText.text = spannable
            } catch (_: Exception) {
                welcomeText.text = "반가워요\n오늘의 컨디션은\n어떤가요?"
            }
        }

        // 항상 서버에서 오늘 약 다시 가져오기
        refreshTodayMedicines(forceRefresh = true)

        preloadAllMedicineImages()
    }

    override fun onResume() {
        super.onResume()

        if (AppState.needRefreshTodayMedicines) {
            refreshTodayMedicines(forceRefresh = true)
            preloadAllMedicineImages()
            AppState.needRefreshTodayMedicines = false
        }
    }

    /** 로딩 중 상태 표시: happy hamster + 로딩 문구 */
    private fun showLoadingTodayMedicines() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE

        val emptyTextView = findViewById<TextView>(R.id.empty_medicine_text)
        val emptyImageView = findViewById<ImageView>(R.id.empty_hamster_image)

        emptyImageView.setImageResource(R.drawable.happy_hamster)
        emptyTextView.text = "오늘의 복용약을 불러오고 있어요.\n잠시만 기다려 주세요."
    }

    /** 실제 리스트/empty 상태 표시 */
    private fun showMedicineList(medicineList: List<MedicineTodayData>) {
        TodayMedicineCache.medicines = medicineList

        if (medicineList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE

            val emptyTextView = findViewById<TextView>(R.id.empty_medicine_text)
            val emptyImageView = findViewById<ImageView>(R.id.empty_hamster_image)

            emptyImageView.setImageResource(R.drawable.shocked_hamster)

            val fullText = "오늘의 복용약이 없어요!\n아직 등록하지 않았나요?"
            val highlight = "오늘의 복용약"
            val spannable = SpannableString(fullText)
            val start = fullText.indexOf(highlight)
            val end = start + highlight.length
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#FF8A65")),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            emptyTextView.text = spannable
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            medicineAdapter.updateItems(medicineList)
        }
    }

    /** 오늘의 약 리스트 갱신 */
    private fun refreshTodayMedicines(forceRefresh: Boolean = false) {
        // 캐시는 무시하고 항상 서버에서 다시 가져옴
        showLoadingTodayMedicines()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getTodayMedicines()
                if (response.isSuccessful) {
                    val baseList = response.body()?.data ?: emptyList<MedicineTodayData>()

                    val listWithImage = baseList.map { todayItem ->
                        val cachedImage = TodayMedicineCache.imageCache[todayItem.medicineName]
                        if (cachedImage != null) {
                            todayItem.copy(image = cachedImage)
                        } else {
                            try {
                                val searchResp =
                                    RetrofitInstance.medicineService.searchMedicines(
                                        medicineName = todayItem.medicineName,
                                        page = 0
                                    )
                                val first = searchResp.data?.firstOrNull()
                                val imageUrl = first?.image
                                TodayMedicineCache.imageCache[todayItem.medicineName] = imageUrl
                                todayItem.copy(image = imageUrl)
                            } catch (_: Exception) {
                                todayItem
                            }
                        }
                    }

                    showMedicineList(listWithImage)
                } else {
                    showMedicineList(emptyList())
                    Toast.makeText(
                        this@HomeActivity,
                        "약 리스트 조회 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                showMedicineList(emptyList())
                Toast.makeText(
                    this@HomeActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /** 등록한 전체 약 기준 이미지 캐시 프리로딩 */
    private fun preloadAllMedicineImages() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitInstance.medicineService.getMedicines(page = 0)

                // resp.data 가 null 일 수 있으므로 ?: emptyList() 로 방어
                if (resp.status != 200) return@launch
                val allList: List<MedicineSimple> = resp.data ?: emptyList()

                allList.forEach { simple ->
                    val name = simple.medicineName
                    if (TodayMedicineCache.imageCache.containsKey(name)) return@forEach

                    try {
                        val searchResp = RetrofitInstance.medicineService.searchMedicines(
                            medicineName = name,
                            page = 0
                        )
                        val first = searchResp.data?.firstOrNull()
                        val imageUrl = first?.image
                        TodayMedicineCache.imageCache[name] = imageUrl
                    } catch (_: Exception) {
                        // 실패한 약은 패스
                    }
                }
            } catch (_: Exception) {
                // 프리로딩 실패해도 홈 UI는 그대로
            }
        }
    }

}
