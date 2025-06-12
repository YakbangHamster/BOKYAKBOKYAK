package com.example.yakbanghamster

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.yakbanghamster.data.MedicationItem
import com.example.yakbanghamster.data.RetrofitInstance
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.model.GradientColor
import kotlinx.coroutines.launch
import kotlin.random.Random

class ZeroHideValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value == 0f) "" else value.toInt().toString()
    }
}

class ReportActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_report

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getMedicineReport()
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        val medicineName = getTopMedicine(data.medication)
                        // 사용자명, 기간 표시 (사용자명만 #FFA655)
                        val username = data.username
                        val reportText =
                            "<font color='#FFA655'>$username</font> 님의<br>주간 레포트를<br>소개할게요"
                        findViewById<TextView>(R.id.userNameText).text = Html.fromHtml(reportText)
                        findViewById<TextView>(R.id.reportPeriodText).text =
                            "${formatDate(data.startDate)} ~ ${formatDate(data.endDate)}"

                        // 요일별 복약 횟수 BarChart
                        val barChart = findViewById<BarChart>(R.id.barChart)
                        val days = listOf("월", "화", "수", "목", "금", "토", "일")
                        val barEntries = data.schedule.mapIndexed { idx: Int, value: Int ->
                            BarEntry(idx.toFloat(), value.toFloat())
                        }
                        val barDataSet = BarDataSet(barEntries, "").apply {
                            gradientColors = List(barEntries.size) {
                                GradientColor(
                                    Color.parseColor("#FFB977"),
                                    Color.parseColor("#FF8F2D")
                                )
                            }
                            valueTextSize = 15f
                            valueTextColor = Color.parseColor("#6F6F6F") // 숫자 색상
                            setDrawValues(true)
                            valueFormatter = ZeroHideValueFormatter()   // 0은 숨김
                        }
                        val barData = BarData(barDataSet).apply {
                            barWidth = 0.6f
                        }
                        barChart.data = barData
                        barChart.setDrawGridBackground(false)
                        barChart.axisLeft.setDrawGridLines(false)
                        barChart.axisRight.setDrawGridLines(false)
                        barChart.xAxis.setDrawGridLines(false)
                        barChart.xAxis.setDrawAxisLine(false)
                        barChart.axisLeft.setDrawAxisLine(false)
                        barChart.axisRight.setDrawAxisLine(false)
                        barChart.axisLeft.axisMinimum = 0f
                        barChart.axisRight.axisMinimum = 0f
                        barChart.axisLeft.setDrawLabels(false) // 왼쪽 값 숨김
                        barChart.axisRight.isEnabled = false
                        barChart.legend.isEnabled = false
                        barChart.description.isEnabled = false
                        barChart.setScaleEnabled(false)
                        barChart.setTouchEnabled(false)
                        barChart.xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            valueFormatter = IndexAxisValueFormatter(days)
                            granularity = 1f
                            labelCount = days.size
                            textSize = 15f
                            textColor = Color.parseColor("#222222")
                        }
                        barChart.setExtraBottomOffset(30f)
                        barChart.invalidate()

                        // 복약 횟수를 가장 잘 지킨 날 표시 (목요일 강조)
                        val maxIdx =
                            data.schedule.indices.maxByOrNull { idx: Int -> data.schedule[idx] }
                                ?: 0
                        val topDay = days[maxIdx]
                        val topDayText =
                            "복약 횟수를 가장 잘 지킨 날은 <font color='#FF8F2D'>${topDay}요일</font>이에요.<br>잘하고 계시네요!"
                        findViewById<TextView>(R.id.topDayText).text = Html.fromHtml(topDayText)

                        // PieChart (복용약별 비율)
                        val pieChart = findViewById<PieChart>(R.id.pieChart)
                        val pieEntries = data.medication.map { item: MedicationItem ->
                            // NaN, 변환 불가, null 모두 0f로 처리
                            PieEntry(
                                item.percent.toFloatOrNull()?.takeIf { !it.isNaN() } ?: 0f,
                                item.medicineName
                            )
                        }

                        fun getRandomPastelColor(): Int {
                            val hue = Random.nextFloat() * 360f
                            val saturation = 0.45f + Random.nextFloat() * 0.15f
                            val value = 0.95f + Random.nextFloat() * 0.05f
                            return Color.HSVToColor(floatArrayOf(hue, saturation, value))
                        }

// 파이차트 조각 개수만큼 파스텔 컬러 리스트 만들기
                        val pastelColors = List(pieEntries.size) { getRandomPastelColor() }

// PieDataSet 생성 및 색상 적용
                        val pieDataSet = PieDataSet(pieEntries, "").apply {
                            colors = pastelColors
                            valueTextSize = 0f
                            sliceSpace = 3f
                        }

// PieChart 설정
                        pieChart.data = PieData(pieDataSet)
                        pieChart.setHoleColor(Color.parseColor("#FFF3E8"))
                        pieChart.holeRadius = 40f
                        pieChart.transparentCircleRadius = 75f
                        pieChart.setDrawEntryLabels(false)
                        pieChart.legend.isEnabled = false
                        pieChart.description.isEnabled = false
                        pieChart.setTouchEnabled(false)
                        pieChart.invalidate()

                        val legendLayout = findViewById<LinearLayout>(R.id.pieLegendLayout)
                        legendLayout.removeAllViews()

                        data.medication.forEachIndexed { idx: Int, item: MedicationItem ->
                            val color = pastelColors[idx]
                            val colorView = View(this@ReportActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                                    rightMargin = 12
                                }
                                background = GradientDrawable().apply {
                                    shape = GradientDrawable.OVAL
                                    setColor(color)
                                }
                            }
                            val nameView = TextView(this@ReportActivity).apply {
                                text = item.medicineName
                                setTextColor(Color.parseColor("#1E232C"))
                                textSize = 14f
                                setPadding(0, 0, 12, 0)
                            }
                            // NaN, 변환불가, null 모두 0%로 표시
                            val percentValue = item.percent.toFloatOrNull()
                            val percentText =
                                if (percentValue == null || percentValue.isNaN()) "0" else item.percent

                            val percentView = TextView(this@ReportActivity).apply {
                                text = "${percentText}%"
                                setTextColor(Color.parseColor("#1E232C"))
                                textSize = 14f
                            }
                            val row = LinearLayout(this@ReportActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = android.view.Gravity.CENTER_VERTICAL
                                setPadding(0, 6, 0, 6)
                                addView(colorView)
                                addView(nameView)
                                addView(percentView)
                            }
                            legendLayout.addView(row)

                        }

                    }
                } // response.isSuccessful 블록 닫기
            } catch (e: Exception) {
                // 에러 처리
            }
        }


        /* 임시 데이터 넣어서 표시
        val username = "김햄찌"
        val startDate = "2025-03-03"
        val endDate = "2025-03-10"
        val schedule = listOf(3, 7, 0, 5, 8, 2, 3) // 월~일
        val medication = listOf(
            MedicationItem("비타민", "60"),
            MedicationItem("고혈압약", "20"),
            MedicationItem("타이레놀", "20")
        )

        // 사용자명, 기간 표시 (사용자명만 #FFA655)
        val reportText =
            "<font color='#FFA655'>$username</font> 님의<br>주간 레포트를<br>소개할게요"
        findViewById<TextView>(R.id.userNameText).text = Html.fromHtml(reportText)
        findViewById<TextView>(R.id.reportPeriodText).text =
            "${formatDate(startDate)} ~ ${formatDate(endDate)}"

        // 요일별 복약 횟수 BarChart
        val barChart = findViewById<BarChart>(R.id.barChart)
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        val barEntries = schedule.mapIndexed { idx: Int, value: Int ->
            BarEntry(idx.toFloat(), value.toFloat())
        }
        val barDataSet = BarDataSet(barEntries, "").apply {
            gradientColors = List(barEntries.size) {
                GradientColor(
                    Color.parseColor("#FF8F2D"),
                    Color.parseColor("#FFB977")
                )
            }
            valueTextSize = 15f
            valueTextColor = Color.parseColor("#6F6F6F")
            setDrawValues(true)
            valueFormatter = ZeroHideValueFormatter()
        }
        val barData = BarData(barDataSet).apply {
            barWidth = 0.6f
        }
        barChart.data = barData
        barChart.setDrawGridBackground(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.setDrawAxisLine(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f
        barChart.axisLeft.setDrawLabels(false)
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(days)
            granularity = 1f
            labelCount = days.size
            textSize = 15f
            textColor = Color.parseColor("#222222")
        }
        barChart.setExtraBottomOffset(30f)
        barChart.invalidate()

        // 복약 횟수를 가장 잘 지킨 날 표시 (목요일 강조)
        val maxIdx = schedule.indices.maxByOrNull { idx: Int -> schedule[idx] } ?: 0
        val topDay = days[maxIdx]
        val topDayText =
            "복약 횟수를 가장 잘 지킨 날은 <font color='#FF8F2D'>${topDay}요일</font>이에요.<br>잘하고 계시네요!"
        findViewById<TextView>(R.id.topDayText).text = Html.fromHtml(topDayText)

        // PieChart (복용약별 비율)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val pieEntries = medication.map { item: MedicationItem ->
            PieEntry(
                item.percent.toFloatOrNull() ?: 0f,
                item.medicineName
            )
        }

        fun getRandomPastelColor(): Int {
            val hue = Random.nextFloat() * 360f
            val saturation = 0.45f + Random.nextFloat() * 0.15f
            val value = 0.95f + Random.nextFloat() * 0.05f
            return Color.HSVToColor(floatArrayOf(hue, saturation, value))
        }

        // 파이차트 조각 개수만큼 파스텔 컬러 리스트 만들기
        val pastelColors = List(pieEntries.size) { getRandomPastelColor() }

        // PieDataSet 생성 및 색상 적용
        val pieDataSet = PieDataSet(pieEntries, "").apply {
            colors = pastelColors
            valueTextSize = 0f
            sliceSpace = 3f
        }

        // PieChart 설정
                pieChart.data = PieData(pieDataSet)
                pieChart.setHoleColor(Color.parseColor("#FFF3E8"))
                pieChart.holeRadius = 40f
                pieChart.transparentCircleRadius = 75f
                pieChart.setDrawEntryLabels(false)
                pieChart.legend.isEnabled = false
                pieChart.description.isEnabled = false
                pieChart.setTouchEnabled(false)
                pieChart.invalidate()

        val legendLayout = findViewById<LinearLayout>(R.id.pieLegendLayout)
        legendLayout.removeAllViews()

        medication.forEachIndexed { idx, item ->
            val color = pastelColors[idx]
            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    rightMargin = 12
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            }
            val nameView = TextView(this).apply {
                text = item.medicineName
                setTextColor(Color.parseColor("#1E232C"))
                textSize = 14f
                setPadding(0, 0, 12, 0)
            }
            val percentView = TextView(this).apply {
                text = "${item.percent}%"
                setTextColor(Color.parseColor("#1E232C"))
                textSize = 14f
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 6, 0, 6)
                addView(colorView)
                addView(nameView)
                addView(percentView)
            }
            legendLayout.addView(row)
        }


        // 주요 텍스트
        val medicineName = getTopMedicine(medication)
        val text = "이번주에 가장 많이 복용한 약은 <font color='#FFA655'>${medicineName}</font>이에요."
        findViewById<TextView>(R.id.topMedicineText).text = Html.fromHtml(text)

        val descText = "이번주에 산정된 ${username} 님의 <font color='#FFA655'>복약순응도</font>예요.\n" +
                "참고로, 만성 질환 노인 환자 중 <font color='#FFA655'>복약순응도</font>가 80% 이상인 비율은 77.9%이라고 해요."
        findViewById<TextView>(R.id.percentDescText).text = Html.fromHtml(descText)
        findViewById<TextView>(R.id.percentText).text = "58%"
    }

         */

    }
}


        private fun formatDate(date: String): String {
            return date.split("-").let {
                "${it[0]}년 ${it[1].toInt()}월 ${it[2].toInt()}일"
            }
        }

        // 가장 많이 복용한 약
        private fun getTopMedicine(medication: List<MedicationItem>): String {
            return medication.maxByOrNull { it.percent.toFloatOrNull() ?: 0f }?.medicineName ?: ""
        }




