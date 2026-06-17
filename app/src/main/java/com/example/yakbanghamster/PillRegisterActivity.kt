package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class PillRegisterActivity : BaseActivity()  {

    override val layoutResId: Int
        get() = R.layout.activity_pill_register

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backButton = findViewById<ImageView>(R.id.btn_back)

        backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val cardScan = findViewById<androidx.cardview.widget.CardView>(R.id.card_scan)
        cardScan.setOnClickListener {
            startActivity(Intent(this, PillOcrActivity::class.java))
        }
    }

    }