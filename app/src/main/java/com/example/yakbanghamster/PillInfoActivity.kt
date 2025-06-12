package com.example.yakbanghamster

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class PillInfoActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }


        val name = intent.getStringExtra("medicineName") ?: ""
        val serial = intent.getStringExtra("medicineSerial") ?: ""
        val efficacy = intent.getStringExtra("medicineEfficacy") ?: ""
        val howToTake = intent.getStringExtra("medicineHowToTake") ?: ""
        val imageUrl = intent.getStringExtra("medicineImage") ?: ""


        val nameText = findViewById<TextView>(R.id.medicineTitle)
        val serialText = findViewById<TextView>(R.id.medicineSub)
        val descriptionText = findViewById<TextView>(R.id.medicineDescription)
        val imageView = findViewById<ImageView>(R.id.medicineImage)


        nameText.text = name
        serialText.text = serial
        descriptionText.text = "[효능/효과]\n$efficacy\n\n[복용법]\n$howToTake"

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.test_pill)
            .into(imageView)
    }
}
