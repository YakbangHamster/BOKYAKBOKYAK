package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout

class PillOcrActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_ocr

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backbutton = findViewById<ImageView>(R.id.btn_back)

        backbutton.setOnClickListener {
            startActivity(Intent(this, PillRegisterActivity::class.java))
            finish()
        }

        val btnGallery = findViewById<LinearLayout>(R.id.btn_gallery)

        btnGallery.setOnClickListener {
            val intent = Intent(this, PillOcrResultActivity::class.java)
            startActivity(intent)
        }
    }
}