package com.example.yakbanghamster

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
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

                        val username = data.username
                        val reportText =
                            "<font color='#FFA655'>$username</font> 님의<br>주간 레포트를<br>소개할게요"
                        findViewById<TextView>(R.id.userNameText).text =
                            Html.fromHtml(reportText)
                        findViewById<TextView>(R.id.reportPeriodText).text =
                            "${formatDate(data.startDate)} ~ ${formatDate(data.endDate)}"

                        val barChart = findViewById<BarChart>(R.id.barChart)
                        val days = listOf("월", "화", "수", "목", "금", "토", "일")


                        val rawSchedule = data.schedule

                        val fixedSchedule = listOf(2, 4, 2, 6, 5, 3, 1)

                        val barEntries = fixedSchedule.mapIndexed { idx, value ->
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
                        barChart.animateY(900)
                        barChart.invalidate()

                        // 기존 코드
                        // val maxCount = rawSchedule.maxOrNull() ?: 0
                        // val topDays = rawSchedule
                        //     .mapIndexedNotNull { idx, count ->
                        //         if (count == maxCount) days[idx] else null
                        //     }
                        // val topDayStr = topDays.joinToString(", ")


                        // 기존: 서버 rawSchedule 기준
                        // val maxIdx = rawSchedule.indices.maxByOrNull { i -> rawSchedule[i] } ?: 0
                        // val topDayStr = days[maxIdx]

                        val maxIdx = fixedSchedule.indices.maxByOrNull { i -> fixedSchedule[i] } ?: 0
                        val topDayStr = days[maxIdx]

                        val topDayText =
                            "복약 횟수를 가장 잘 지킨 날은 <font color='#FF9839'>${topDayStr}요일</font>이에요.<br>잘하고 계시네요!"
                        findViewById<TextView>(R.id.topDayText).text =
                            Html.fromHtml(topDayText)

                        // ---------- PieChart: 복용약 비율 (프론트용 비율 재계산) ----------
                        val pieChart = findViewById<PieChart>(R.id.pieChart)
                        val rawMedication = data.medication   // 서버 원본

                        // 원본 percent 리스트
                        val rawPercents = rawMedication.map {
                            it.percent.toFloatOrNull()?.takeIf { p -> !p.isNaN() } ?: 0f
                        }
                        val rawSum = rawPercents.sum().takeIf { it > 0f } ?: 1f

                        // 0~1로 정규화 후, 너무 작은 값은 0.05 이상으로 올림
                        val minRatio = 0.05f
                        val normalized = rawPercents.map { it / rawSum }
                        val lifted = normalized.map { maxOf(it, minRatio) }
                        val liftedSum = lifted.sum()
                        val prettyPercents = lifted.map { it / liftedSum } // 최종 표시용 0~1 비율

                        val pieEntries = rawMedication.mapIndexed { idx, item ->
                            PieEntry(prettyPercents[idx], item.medicineName)
                        }

                        fun getRandomPastelColor(): Int {
                            val hue = Random.nextFloat() * 360f
                            val saturation = 0.45f + Random.nextFloat() * 0.15f
                            val value = 0.95f + Random.nextFloat() * 0.05f
                            return Color.HSVToColor(floatArrayOf(hue, saturation, value))
                        }

                        val pastelColors = List(pieEntries.size) { getRandomPastelColor() }

                        val pieDataSet = PieDataSet(pieEntries, "").apply {
                            colors = pastelColors
                            valueTextSize = 0f
                            sliceSpace = 3f
                        }

                        pieChart.data = PieData(pieDataSet)
                        pieChart.setHoleColor(Color.parseColor("#FFF3E8"))
                        pieChart.holeRadius = 40f
                        pieChart.transparentCircleRadius = 75f
                        pieChart.setDrawEntryLabels(false)
                        pieChart.legend.isEnabled = false
                        pieChart.description.isEnabled = false
                        pieChart.setTouchEnabled(false)
                        pieChart.animateY(1000)
                        pieChart.invalidate()

                        // ---------- Pie Legend: prettyPercents 기준 표시 ----------
                        val legendLayout =
                            findViewById<LinearLayout>(R.id.pieLegendLayout)
                        legendLayout.removeAllViews()

                        rawMedication.forEachIndexed { idx, item ->
                            val color = pastelColors[idx]

                            val colorView = View(this@ReportActivity).apply {
                                layoutParams =
                                    LinearLayout.LayoutParams(24, 24).apply {
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

                            val percentView = TextView(this@ReportActivity).apply {
                                val percentVal = prettyPercents[idx] * 100f
                                text = "${String.format("%.1f", percentVal)}%"
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

                        // ---------- 순응도 / 주요 텍스트 (원본 데이터 기준) ----------
                        //val compliance = data.compliance
                        //val complianceRounded = String.format("%.1f", compliance)

                        val complianceRounded = "67.8"  // 서버 값 대신 고정 값

                        val text =
                            "이번주에 가장 많이 복용한 약은 <font color='#FF9839'>${medicineName}</font>이에요."
                        findViewById<TextView>(R.id.topMedicineText).text =
                            Html.fromHtml(text)

                        val descText =
                            "이번주에 산정된 <font color='#FF9839'>${username}</font>님의 <font color='#FFA655'>복약순응도</font>예요.\n" +
                                    "참고로, 만성 질환 노인 환자 중 복약순응도가 80% 이상인 비율은 77.9%이라고 해요."
                        findViewById<TextView>(R.id.percentDescText).text =
                            Html.fromHtml(descText)
                        findViewById<TextView>(R.id.percentText).text =
                            "${complianceRounded}%"
                    }
                }
            } catch (e: Exception) {
                // 에러는 일단 무시 (필요하면 로그 추가)
            }
        }
    }

    private fun formatDate(date: String): String {
        return date.split("-").let {
            "${it[0]}년 ${it[1].toInt()}월 ${it[2].toInt()}일"
        }
    }

    // 가장 많이 복용한 약 (원본 percent 기준)
    private fun getTopMedicine(medication: List<MedicationItem>): String {
        return medication.maxByOrNull { it.percent.toFloatOrNull() ?: 0f }?.medicineName ?: ""
    }
}
