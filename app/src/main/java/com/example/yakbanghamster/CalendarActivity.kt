package com.example.yakbanghamster

import MedicineDetailAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.data.ConditionResponse
import com.example.yakbanghamster.data.RetrofitInstance
import com.prolificinteractive.materialcalendarview.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

class CalendarActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_calendar

    private lateinit var calendarView: MaterialCalendarView
    private var isWeekMode = true
    private var selectedDate: String = LocalDate.now().toString()
    private lateinit var conditionGuideText: TextView
    private lateinit var medicineAdapter: MedicineDetailAdapter
    private lateinit var btnTakePill: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        calendarView = findViewById(R.id.calendarView)
        // 1. 주간 모드, 첫 요일 일요일로 명시적 설정
        calendarView.state().edit()
            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit()

        // 2. 헤더(월/연도, 화살표) 숨기기
        calendarView.topbarVisible = false

        // 3. 한글 요일 커스텀 및 일/토요일 색상 변경
        calendarView.setWeekDayFormatter { dayOfWeek: DayOfWeek ->
            val days = listOf("일", "월", "화", "수", "목", "금", "토")
            days[dayOfWeek.value % 7]
        }

        // 4. 오늘 날짜 자동 선택
        calendarView.selectedDate = CalendarDay.from(LocalDate.now())

        // 5. 요일별 날짜 색상 커스텀 (Decorator 사용)
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                val dow = day.date.dayOfWeek
                return dow == DayOfWeek.SUNDAY || dow == DayOfWeek.SATURDAY
            }

            override fun decorate(view: DayViewFacade) {
                view.addSpan(object : android.text.style.ForegroundColorSpan(Color.GRAY) {})
            }
        })

        // 6. 주간/월간 토글
        findViewById<ImageView>(R.id.pill_calendar).setOnClickListener {
            isWeekMode = !isWeekMode
            calendarView.state().edit()
                .setCalendarDisplayMode(
                    if (isWeekMode) CalendarMode.WEEKS else CalendarMode.MONTHS
                )
                .commit()
        }

        // 7. 이전/다음 달 이동
        findViewById<ImageView>(R.id.btn_prev_month).setOnClickListener {
            calendarView.goToPrevious()
        }
        findViewById<ImageView>(R.id.btn_next_month).setOnClickListener {
            calendarView.goToNext()
        }

        // 8. 년 월 자동 수정
        calendarView.setOnMonthChangedListener { widget, date ->
            val year = date.year
            val month = date.month
            findViewById<TextView>(R.id.tv_date).text = "${year}년 ${month}월"
        }

        // 10. 컨디션 LinearLayout 찾기 및 클릭 리스너
        val refreshed = findViewById<LinearLayout>(R.id.img_refreshed)
        val lively = findViewById<LinearLayout>(R.id.img_lively)
        val sleepy = findViewById<LinearLayout>(R.id.img_sleepy)
        val tired = findViewById<LinearLayout>(R.id.img_tired)

        conditionGuideText = findViewById(R.id.tv_condition_guide)


        calendarView.setOnDateChangedListener { widget, date, selected ->
            selectedDate = date.date.toString()
            // 클릭시 컨디션 조회
            getCondition(selectedDate) { condition: ConditionResponse? ->
                clearAllConditionBorders()
                if (condition?.status == 200 && condition.data != null) {
                    setEmojiBorderByCode(condition.data)
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    conditionGuideText.text = "오늘의 컨디션을 선택해 보세요 :D"
                }
            }
            // 약 리스트/상세 조회도 같이 호출
            loadMedicinesForDate(selectedDate)
        }


// 클릭 시 무조건 POST → 성공하면 문구와 테두리 갱신, 실패 시 안내
        refreshed.setOnClickListener {
            postCondition(selectedDate, "01") { success ->
                if (success) {
                    setEmojiBorderByCode("01")
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    conditionGuideText.text = "컨디션 등록에 실패했습니다."
                }
            }
        }
        lively.setOnClickListener {
            postCondition(selectedDate, "02") { success ->
                if (success) {
                    setEmojiBorderByCode("02")
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    conditionGuideText.text = "컨디션 등록에 실패했습니다."
                }
            }
        }
        sleepy.setOnClickListener {
            postCondition(selectedDate, "03") { success ->
                if (success) {
                    setEmojiBorderByCode("03")
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    conditionGuideText.text = "컨디션 등록에 실패했습니다."
                }
            }
        }
        tired.setOnClickListener {
            postCondition(selectedDate, "04") { success ->
                if (success) {
                    setEmojiBorderByCode("04")
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    conditionGuideText.text = "컨디션 등록에 실패했습니다."
                }
            }
        }


        // RecyclerView, Adapter 연결
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_medicine)
        medicineAdapter = MedicineDetailAdapter(emptyList())
        recyclerView.adapter = medicineAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnTakePill = findViewById(R.id.btn_take_pill)

        val today = LocalDate.now().toString()

        getCondition(today) { condition: ConditionResponse? ->
            clearAllConditionBorders()
            if (condition?.status == 200 && condition.data != null) {
                setEmojiBorderByCode(condition.data)
                conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
            } else {
                conditionGuideText.text = "오늘의 컨디션을 선택해 보세요 :D"
            }
        }
        loadMedicinesForDate(today)
    }




    // 컨디션 등록/수정/조회/ API 함수
    private fun postCondition(date: String, emojiCode: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.postCondition(
                    mapOf("date" to date, "emojiCode" to emojiCode)
                )
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    private fun updateCondition(date: String, emojiCode: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.updateCondition(
                    mapOf("date" to date, "emojiCode" to emojiCode)
                )
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }


    private fun getCondition(date: String, onResult: (ConditionResponse?) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.getCondition(date)
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    // 테두리 처리 함수
    private fun clearAllConditionBorders() {
        findViewById<ImageView>(R.id.iv_refreshed).background = null
        findViewById<ImageView>(R.id.iv_lively).background = null
        findViewById<ImageView>(R.id.iv_sleepy).background = null
        findViewById<ImageView>(R.id.iv_tired).background = null
    }

    private fun setEmojiBorderByCode(emojiCode: String) {
        clearAllConditionBorders()
        val imageViewId = when (emojiCode) {
            "01" -> R.id.iv_refreshed
            "02" -> R.id.iv_lively
            "03" -> R.id.iv_sleepy
            "04" -> R.id.iv_tired
            else -> null
        }
        imageViewId?.let {
            findViewById<ImageView>(it).background =
                ContextCompat.getDrawable(this, R.drawable.condition_selected_border)
        }
    }


    private fun loadMedicinesForDate(selectedDate: String) {
        lifecycleScope.launch {
            try {
                val listResponse = RetrofitInstance.medicineService.getMedicines(0)
                val filtered = listResponse.data.filter { it.date == selectedDate }
                Log.d("약필터", "필터링된 약: ${filtered.joinToString { it.medicineName }}")

                val detailList = coroutineScope {
                    filtered.map { item ->
                        async {
                            val resp =
                                RetrofitInstance.medicineService.getMedicineDetail(item.medicineName)
                            resp.body()?.data
                        }
                    }.awaitAll().filterNotNull()
                }

                medicineAdapter.updateItems(detailList)
                Log.d("약상세", "상세 개수: ${detailList.size}")
                // ★ 버튼 표시/숨김 처리
                btnTakePill.visibility = if (detailList.isNotEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Log.e("약조회", "에러: ${e.message}")
                medicineAdapter.updateItems(emptyList())
                btnTakePill.visibility = View.GONE
            }
        }

    }



}



