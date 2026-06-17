package com.example.yakbanghamster

import MedicineDetailAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class CalendarActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_calendar

    private lateinit var calendarView: MaterialCalendarView
    private var isWeekMode = true
    private var selectedDate: String = LocalDate.now().toString()
    private var currentConditionCode: String? = null
    private lateinit var conditionGuideText: TextView
    private lateinit var medicineAdapter: MedicineDetailAdapter
    private lateinit var btnTakePill: Button
    private val REQUEST_VIDEO_PICK = 1001


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


        // 날짜 클릭 시마다 getCondition을 반드시 호출
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // 항상 yyyy-MM-dd 포맷으로 변환
            selectedDate = date.date.toString()
            Log.d("CalendarActivity", "selectedDate: $selectedDate")
            getCondition(selectedDate) { condition: ConditionResponse? ->
                clearAllConditionBorders()
                if (condition?.status == 200 && condition.data != null) {
                    setEmojiBorderByCode(condition.data)
                    currentConditionCode = condition.data
                    conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                } else {
                    currentConditionCode = null
                    conditionGuideText.text = "오늘의 컨디션을 선택해 보세요 :D"
                }
            }
            loadMedicinesForDate(selectedDate)
        }

        // 앱 시작 시 오늘 날짜 컨디션도 반드시 조회
        getCondition(selectedDate) { condition: ConditionResponse? ->
            clearAllConditionBorders()
            if (condition?.status == 200 && condition.data != null) {
                setEmojiBorderByCode(condition.data)
                currentConditionCode = condition.data
                conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
            } else {
                currentConditionCode = null
                conditionGuideText.text = "오늘의 컨디션을 선택해 보세요 :D"
            }
        }
        loadMedicinesForDate(selectedDate)


        // 각 컨디션 이모지 클릭 시 토글 동작
        refreshed.setOnClickListener {
            toggleCondition(selectedDate, "01") { success, action ->
                if (success) {
                    when (action) {
                        "register" -> {
                            setEmojiBorderByCode("01")
                            conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                        }
                        "update" -> {
                            setEmojiBorderByCode("01")
                            conditionGuideText.text = "오늘의 컨디션이 수정되었어요!"
                        }
                        "delete" -> {
                            clearAllConditionBorders()
                            conditionGuideText.text = "오늘의 컨디션이 삭제되었어요!"
                        }
                    }
                } else {
                    conditionGuideText.text = "컨디션 처리에 실패했습니다."
                }
            }

        }
        lively.setOnClickListener {
            toggleCondition(selectedDate, "02") { success, action ->
                if (success) {
                    when (action) {
                        "register" -> {
                            setEmojiBorderByCode("02")
                            conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                        }
                        "update" -> {
                            setEmojiBorderByCode("02")
                            conditionGuideText.text = "오늘의 컨디션이 수정되었어요!"
                        }
                        "delete" -> {
                            clearAllConditionBorders()
                            conditionGuideText.text = "오늘의 컨디션이 삭제되었어요!"
                        }
                    }
                } else {
                    conditionGuideText.text = "컨디션 처리에 실패했습니다."
                }
            }

        }
        sleepy.setOnClickListener {
            toggleCondition(selectedDate, "03") { success, action ->
                if (success) {
                    when (action) {
                        "register" -> {
                            setEmojiBorderByCode("03")
                            conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                        }
                        "update" -> {
                            setEmojiBorderByCode("03")
                            conditionGuideText.text = "오늘의 컨디션이 수정되었어요!"
                        }
                        "delete" -> {
                            clearAllConditionBorders()
                            conditionGuideText.text = "오늘의 컨디션이 삭제되었어요!"
                        }
                    }
                } else {
                    conditionGuideText.text = "컨디션 처리에 실패했습니다."
                }
            }

        }
        tired.setOnClickListener {
            toggleCondition(selectedDate, "04") { success, action ->
                if (success) {
                    when (action) {
                        "register" -> {
                            setEmojiBorderByCode("04")
                            conditionGuideText.text = "오늘의 컨디션이 등록되었어요!"
                        }
                        "update" -> {
                            setEmojiBorderByCode("04")
                            conditionGuideText.text = "오늘의 컨디션이 수정되었어요!"
                        }
                        "delete" -> {
                            clearAllConditionBorders()
                            conditionGuideText.text = "오늘의 컨디션이 삭제되었어요!"
                        }
                    }
                } else {
                    conditionGuideText.text = "컨디션 처리에 실패했습니다."
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



        // 복용 시작 버튼 클릭 리스너
        btnTakePill.setOnClickListener {
            val intent = Intent(this, RealTimeDetectionActivity::class.java)
            startActivity(intent)
        }



    }




    // 컨디션 등록(POST)
    private fun postCondition(date: String, emojiCode: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.postCondition(
                    mapOf("date" to date, "emojiCode" to emojiCode)
                )
                if (response.isSuccessful) currentConditionCode = emojiCode
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // 컨디션 수정(PATCH)
    private fun updateCondition(date: String, emojiCode: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.patchCondition(
                    mapOf("date" to date, "emojiCode" to emojiCode)
                )
                if (response.isSuccessful) currentConditionCode = emojiCode
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // 컨디션 삭제(DELETE)
    private fun deleteCondition(date: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.deleteCondition(
                    mapOf("date" to date)
                )
                if (response.isSuccessful) currentConditionCode = null
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // 컨디션 조회(GET)
    private fun getCondition(date: String, onResult: (ConditionResponse?) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.conditionService.getCondition(date)
                if (response.isSuccessful) {
                    currentConditionCode = response.body()?.data
                    onResult(response.body())
                } else {
                    currentConditionCode = null
                    onResult(null)
                }
            } catch (e: Exception) {
                currentConditionCode = null
                onResult(null)
            }
        }
    }

    private fun toggleCondition(
        date: String,
        emojiCode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (currentConditionCode == null) {
            // 등록(POST)
            postCondition(date, emojiCode) { success ->
                if (success) {
                    setEmojiBorderByCode(emojiCode)
                    currentConditionCode = emojiCode
                }
                onResult(success, "register")
            }
        } else if (currentConditionCode == emojiCode) {
            // 삭제(DELETE)
            deleteCondition(date) { success ->
                if (success) {
                    clearAllConditionBorders()
                    currentConditionCode = null
                }
                onResult(success, "delete")
            }
        } else {
            // 수정(PATCH)
            updateCondition(date, emojiCode) { success ->
                if (success) {
                    setEmojiBorderByCode(emojiCode)
                    currentConditionCode = emojiCode
                }
                onResult(success, "update")
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
                // endDate 범위 내 포함되는 약만 필터링
                val filtered = listResponse.data.filter { medicine ->
                    val startDate = medicine.startDate
                    val endDate = medicine.endDate

                    if (startDate != null && endDate != null) {
                        val start = LocalDate.parse(startDate)
                        val end = LocalDate.parse(endDate)
                        val current = LocalDate.parse(selectedDate)
                        current in start..end  // 범위 체크!
                    } else if (startDate == null && endDate == null) {
                        // 무기한 약 - 항상 표시
                        true
                    } else {
                        // startDate만 있거나 endDate만 있는 경우
                        startDate == selectedDate
                    }
                }

                Log.d("약필터", "필터링된 약 (${filtered.size}): ${filtered.joinToString { "${it.medicineName}(${it.startDate} ~ ${it.endDate})" }}")

                val detailList = coroutineScope {
                    filtered.map { item ->
                        async {
                            val resp = RetrofitInstance.medicineService.getMedicineDetail(item.medicineName)
                            resp.body()?.data
                        }
                    }.awaitAll().filterNotNull()
                }

                medicineAdapter.updateItems(detailList)
                Log.d("약상세", "상세 개수: ${detailList.size}")

                // 약이 있을 때만 버튼과 텍스트 표시
                if (detailList.isNotEmpty()) {
                    btnTakePill.visibility = View.VISIBLE
                    // "오늘의 복용약이에요" 텍스트 View ID를 찾아서 표시
                    findViewById<TextView>(R.id.tv_today_medicines)?.visibility = View.VISIBLE
                } else {
                    btnTakePill.visibility = View.GONE
                    // 약 없을 때 텍스트 숨김
                    findViewById<TextView>(R.id.tv_today_medicines)?.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e("약조회", "에러: ${e.message}")
                medicineAdapter.updateItems(emptyList())
                btnTakePill.visibility = View.GONE
                // 에러시에도 텍스트 숨김
                findViewById<TextView>(R.id.tv_today_medicines)?.visibility = View.GONE
            }
        }
    }



}



