package com.example.yakbanghamster

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView

abstract class BaseActivity : AppCompatActivity() {
    abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        findViewById<ComposeView>(R.id.compose_nav_bar)?.setContent {
            val menuItems = listOf(
                BottomBarMenuItem(R.drawable.home, "홈"),
                BottomBarMenuItem(R.drawable.clipboard, "약물수첩"),
                BottomBarMenuItem(R.drawable.alarm, "복약알림"),
                BottomBarMenuItem(R.drawable.profile, "마이페이지")
            )
            var selected by remember { mutableStateOf(0) }
            BottomBarWithCenterFab(
                menuItems = menuItems,
                selectedIndex = selected,
                onMenuClick = { index ->
                    selected = index
                    when (index) {
                        0 -> startActivity(Intent(this, HomeActivity::class.java))
                        1 -> startActivity(Intent(this, PillDiaryActivity::class.java))
                    }
                },
                onFabClick = {
                    if (this !is CalendarActivity) {
                        startActivity(Intent(this, CalendarActivity::class.java))
                    }
                }

            )
        }
    }
}
