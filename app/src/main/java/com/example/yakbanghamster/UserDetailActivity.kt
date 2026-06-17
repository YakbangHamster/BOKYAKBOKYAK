package com.example.yakbanghamster.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.R
import com.example.yakbanghamster.ui.fragment.UserDetail1Fragment

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // 프래그먼트 표시
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserDetail1Fragment())
            .commit()
    }
}
