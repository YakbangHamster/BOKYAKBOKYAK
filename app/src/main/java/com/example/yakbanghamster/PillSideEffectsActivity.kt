package com.example.yakbanghamster

import MedicineSimpleAdapter
import SideEffectBottomSheet
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.yakbanghamster.data.RetrofitInstance
import kotlinx.coroutines.launch

class PillSideEffectsActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_side_effects

    private lateinit var adapter: MedicineSimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backbutton = findViewById<ImageView>(R.id.btn_back)
        backbutton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.medicineRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MedicineSimpleAdapter(emptyList()) { medicine ->
            val sheet = SideEffectBottomSheet(medicine.medicineName)
            sheet.show(supportFragmentManager, "SideEffectBottomSheet")
        }
        recyclerView.adapter = adapter



        val userNameText = findViewById<TextView>(R.id.userNameText)
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.userService.getUserName()
                val name = response.data

                val message = "$name 님의\n복용약은 다음과 같아요"

                val spannable = SpannableString(message)
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFA655")),
                    0,
                    name.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                userNameText.text = spannable
            } catch (e: Exception) {
                userNameText.text = "복용약은 다음과 같아요"
            }
        }


        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.medicineService.getMedicines(0)
                Log.d("PillSideEffectsActivity", "받은 데이터: ${response.data}")
                adapter.updateItems(response.data)
            } catch (e: Exception) {
                Toast.makeText(
                    this@PillSideEffectsActivity,
                    "데이터 불러오기 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}

