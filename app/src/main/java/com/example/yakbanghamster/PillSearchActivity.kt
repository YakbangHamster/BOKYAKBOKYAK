package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

class PillSearchActivity : BaseActivity() {


    override val layoutResId: Int
        get() = R.layout.activity_pill_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backButton = findViewById<ImageView>(R.id.btn_back)
        val editSearch = findViewById<EditText>(R.id.edit_search)
        val btnSearch = findViewById<Button>(R.id.btn_search)


        backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnSearch.setOnClickListener {
            val keyword = editSearch.text.toString().trim()
            if (keyword.isNotEmpty()) {
                val intent = Intent(this, PillListActivity::class.java)
                intent.putExtra("search_keyword", keyword)
                startActivity(intent)
            }
        }

    }


}
