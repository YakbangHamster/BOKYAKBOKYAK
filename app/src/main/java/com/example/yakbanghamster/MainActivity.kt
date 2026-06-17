package com.example.yakbanghamster
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbanghamster.ui.fragment.UserDetail1Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserDetail1Fragment())
                .commit()
        }

    }
}