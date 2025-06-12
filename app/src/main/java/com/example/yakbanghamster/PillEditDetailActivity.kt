package com.example.yakbanghamster

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PillEditDetailActivity : BaseActivity() {
    override val layoutResId: Int
        get() = R.layout.activity_pill_edit_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        val name = intent.getStringExtra("medicine_name") ?: ""
        Log.d("PillEditDetailActivity", "받은 이름: $name")
        val editSearch = findViewById<EditText>(R.id.edit_search)
        editSearch.setText(name)

        val toggleButtons = listOf(
            findViewById<ToggleButton>(R.id.btn_mon),
            findViewById<ToggleButton>(R.id.btn_tue),
            findViewById<ToggleButton>(R.id.btn_wed),
            findViewById<ToggleButton>(R.id.btn_thu),
            findViewById<ToggleButton>(R.id.btn_fri),
            findViewById<ToggleButton>(R.id.btn_sat),
            findViewById<ToggleButton>(R.id.btn_sun)
        )
        for (toggle in toggleButtons) {
            toggle.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    button.setBackgroundResource(R.drawable.search_chip_bg)
                    button.setTextColor(Color.parseColor("#FFA450"))
                } else {
                    button.setBackgroundResource(R.drawable.edittext_bg)
                    button.setTextColor(Color.parseColor("#8391A1"))
                }
            }
        }

        val editStartDate = findViewById<EditText>(R.id.edit_start_date)
        val editEndDate = findViewById<EditText>(R.id.edit_end_date)
        val btnCalendarStart = findViewById<ImageView>(R.id.btn_calendar_start)
        val btnCalendarEnd = findViewById<ImageView>(R.id.btn_calendar_end)

        val today = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val todayStr = sdf.format(today)
        editStartDate.setText(todayStr)
        editEndDate.setText(todayStr)

        btnCalendarStart.setOnClickListener { showDatePicker(editStartDate) }
        btnCalendarEnd.setOnClickListener { showDatePicker(editEndDate) }

        val btnPlus = findViewById<ImageView>(R.id.btn_plus)
        val btnMinus = findViewById<ImageView>(R.id.btn_minus)
        val textCount = findViewById<TextView>(R.id.text_count)
        val timeInputContainer = findViewById<LinearLayout>(R.id.time_input_container)
        textCount.text = "1"

        fun setupTimeInputView(timeInput: LinearLayout) {
            val editHour = timeInput.findViewById<EditText>(R.id.edit_hour)
            val editMinute = timeInput.findViewById<EditText>(R.id.edit_minute)
            val btnHourUp = timeInput.findViewById<ImageView>(R.id.btn_hour_up)
            val btnHourDown = timeInput.findViewById<ImageView>(R.id.btn_hour_down)
            val btnMinuteUp = timeInput.findViewById<ImageView>(R.id.btn_minute_up)
            val btnMinuteDown = timeInput.findViewById<ImageView>(R.id.btn_minute_down)
            val textAmPm = timeInput.findViewById<TextView>(R.id.text_ampm)

            editHour.filters = arrayOf(InputFilter.LengthFilter(2))
            editMinute.filters = arrayOf(InputFilter.LengthFilter(2))

            btnHourUp.setOnClickListener {
                val h = editHour.text.toString().toIntOrNull() ?: 12
                val newH = if (h >= 12) 1 else h + 1
                editHour.setText(String.format("%02d", newH))
            }
            btnHourDown.setOnClickListener {
                val h = editHour.text.toString().toIntOrNull() ?: 12
                val newH = if (h <= 1) 12 else h - 1
                editHour.setText(String.format("%02d", newH))
            }
            btnMinuteUp.setOnClickListener {
                val m = editMinute.text.toString().toIntOrNull() ?: 0
                val newM = if (m >= 59) 0 else m + 1
                editMinute.setText(String.format("%02d", newM))
            }
            btnMinuteDown.setOnClickListener {
                val m = editMinute.text.toString().toIntOrNull() ?: 0
                val newM = if (m <= 0) 59 else m - 1
                editMinute.setText(String.format("%02d", newM))
            }
            textAmPm.setOnClickListener {
                textAmPm.text = if (textAmPm.text == "AM") "PM" else "AM"
            }
            editHour.doAfterTextChanged {
                val h = editHour.text.toString().toIntOrNull() ?: 12
                if (h < 1 || h > 12) editHour.setText("12")
            }
            editMinute.doAfterTextChanged {
                val m = editMinute.text.toString().toIntOrNull() ?: 0
                if (m < 0 || m > 59) editMinute.setText("00")
            }
        }

        fun updateTimeInputs(count: Int) {
            timeInputContainer.removeAllViews()
            for (i in 0 until count) {
                val timeInput = layoutInflater.inflate(
                    R.layout.item_time_picker,
                    timeInputContainer,
                    false
                ) as LinearLayout
                setupTimeInputView(timeInput)
                timeInputContainer.addView(timeInput)
            }
        }

        updateTimeInputs(1)

        btnPlus.setOnClickListener {
            val currentCount = textCount.text.toString().toIntOrNull() ?: 1
            val newCount = currentCount + 1
            textCount.text = newCount.toString()
            updateTimeInputs(newCount)
        }
        btnMinus.setOnClickListener {
            val currentCount = textCount.text.toString().toIntOrNull() ?: 1
            if (currentCount > 1) {
                val newCount = currentCount - 1
                textCount.text = newCount.toString()
                updateTimeInputs(newCount)
            }
        }

        val btnEdit = findViewById<Button>(R.id.btn_edit)

        fun to24HourFormat(hourStr: String, minuteStr: String, ampm: String): String {
            val hour = hourStr.toIntOrNull() ?: 12
            val minute = minuteStr.toIntOrNull() ?: 0
            val hour24 = when {
                ampm == "AM" && hour == 12 -> 0
                ampm == "AM" -> hour
                ampm == "PM" && hour == 12 -> 12
                ampm == "PM" -> hour + 12
                else -> hour
            }
            return String.format("%02d:%02d", hour24, minute)
        }

        fun to12HourFormat(hour24: Int): Pair<String, String> {
            val ampm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return hour12.toString().padStart(2, '0') to ampm
        }


        btnEdit.setOnClickListener {
            val medName = editSearch.text.toString()
            Log.d("PillEditDetailActivity", "입력된 약 이름: $medName")

            val schedule = toggleButtons.mapIndexed { idx, btn ->
                val checked = if (btn.isChecked) 1 else 0
                Log.d("PillEditDetailActivity", "요일[$idx] isChecked: ${btn.isChecked} -> $checked")
                checked
            }

            val inputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDateRaw = editStartDate.text.toString()
            val endDateRaw = editEndDate.text.toString()
            val startDateFormatted = try {
                outputFormat.format(inputFormat.parse(startDateRaw)!!)
            } catch (e: Exception) {
                Log.e("PillEditDetailActivity", "시작일 변환 오류: ${e.message}")
                startDateRaw
            }
            val endDateFormatted = try {
                outputFormat.format(inputFormat.parse(endDateRaw)!!)
            } catch (e: Exception) {
                Log.e("PillEditDetailActivity", "종료일 변환 오류: ${e.message}")
                endDateRaw
            }
            Log.d("PillEditDetailActivity", "시작일: $startDateFormatted, 종료일: $endDateFormatted")

            val number = textCount.text.toString().toIntOrNull() ?: 1
            Log.d("PillEditDetailActivity", "복용 횟수: $number")

            val timeList = mutableListOf<String>()
            for (i in 0 until timeInputContainer.childCount) {
                val timeView = timeInputContainer.getChildAt(i)
                val editHour = timeView.findViewById<EditText>(R.id.edit_hour)
                val editMinute = timeView.findViewById<EditText>(R.id.edit_minute)
                val textAmPm = timeView.findViewById<TextView>(R.id.text_ampm)
                val hourStr = editHour.text.toString()
                val minuteStr = editMinute.text.toString()
                val ampm = textAmPm.text.toString()
                val time24 = to24HourFormat(hourStr, minuteStr, ampm)
                Log.d("PillEditDetailActivity", "time[$i]: $hourStr:$minuteStr $ampm -> $time24")
                timeList.add(time24)
            }

            val json = JSONObject().apply {
                put("name", medName)
                put("schedule", JSONArray(schedule))
                put("startDate", startDateFormatted)
                put("endDate", endDateFormatted)
                put("number", number)
                put("time", JSONArray(timeList))
            }

            Log.d("PillEditDetailActivity", "PATCH 요청 JSON: ${json}")

            lifecycleScope.launch {
                try {
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val response = RetrofitInstance.medicineService.patchMedicineDetail(body)
                    Log.d("PillEditDetailActivity", "PATCH 응답 코드: ${response.code()}")
                    val responseBody = response.body()?.string()
                    Log.d("PillEditDetailActivity", "PATCH 응답 바디: $responseBody")
                    if (response.isSuccessful) {
                        Toast.makeText(this@PillEditDetailActivity, "수정 성공!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@PillEditDetailActivity, "수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("PillEditDetailActivity", "수정 오류: ${e.message}", e)
                    Toast.makeText(this@PillEditDetailActivity, "수정 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getMedicineDetail(name)

                if (response.isSuccessful) {
                    val detail = response.body()?.data
                    if (detail != null) {

                        Log.d("PillEditDetailActivity", "이름: ${detail.name}")
                        Log.d(
                            "PillEditDetailActivity",
                            "시작일: ${detail.startDate}, 종료일: ${detail.endDate}"
                        )
                        Log.d("PillEditDetailActivity", "복용횟수: ${detail.number}")
                        Log.d("PillEditDetailActivity", "요일: ${detail.schedule}")
                        Log.d("PillEditDetailActivity", "시간: ${detail.time}")
                        // 이름
                        editSearch.setText(detail.name)
                        Log.d("PillEditDetailActivity", "editSearch 값: ${editSearch.text}")

                        // 날짜
                        editStartDate.setText(detail.startDate.replace("-", "."))
                        editEndDate.setText(detail.endDate.replace("-", "."))
                        Log.d(
                            "PillEditDetailActivity",
                            "editStartDate 값: ${editStartDate.text}, editEndDate 값: ${editEndDate.text}"
                        )

                        // 복용 횟수
                        textCount.setText(detail.number.toString())
                        Log.d("PillEditDetailActivity", "textCount 값: ${textCount.text}")

                        // 요일 토글
                        toggleButtons.forEachIndexed { idx, btn ->
                            btn.isChecked = detail.schedule.getOrNull(idx) == true
                            Log.d(
                                "PillEditDetailActivity",
                                "toggleButton[$idx] isChecked: ${btn.isChecked}"
                            )
                        }

                        // 시간 입력 필드 동적 생성 및 값 세팅
                        timeInputContainer.removeAllViews()
                        for ((i, time) in detail.time.withIndex()) {
                            val timeView = layoutInflater.inflate(
                                R.layout.item_time_picker,
                                timeInputContainer,
                                false
                            )
                            val (hour, minute) = time.split(":")
                            val hourInt = hour.toIntOrNull() ?: 0
                            val (displayHour, ampm) = to12HourFormat(hourInt)
                            timeView.findViewById<EditText>(R.id.edit_hour).setText(displayHour)
                            timeView.findViewById<EditText>(R.id.edit_minute).setText(minute)
                            timeView.findViewById<TextView>(R.id.text_ampm).text = ampm
                            timeInputContainer.addView(timeView)
                            Log.d(
                                "PillEditDetailActivity",
                                "timeInput[$i]: $displayHour:$minute $ampm"
                            )
                        }


                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PillEditDetailActivity,
                    "등록된 상세 정보가 존재하지 않아요!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showDatePicker(targetEditText: EditText) {
        val cal = Calendar.getInstance()
        val current = try {
            SimpleDateFormat(
                "yyyy.MM.dd",
                Locale.getDefault()
            ).parse(targetEditText.text.toString())
        } catch (e: Exception) {
            null
        }
        if (current != null) cal.time = current
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { _, y, m, d ->
            val dateStr = "%04d.%02d.%02d".format(y, m + 1, d)
            targetEditText.setText(dateStr)
        }, year, month, day).show()
    }


}
