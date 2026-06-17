package com.example.yakbanghamster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val root = findViewById<ConstraintLayout>(R.id.rootWelcome)
        val pill = findViewById<ImageView>(R.id.pillImage)
        val name = findViewById<TextView>(R.id.tvAppName)
        val sub  = findViewById<TextView>(R.id.tvSubtitle)
        val btn  = findViewById<MaterialButton>(R.id.startButton)

        // 버튼 클릭 시 로그인 화면으로 이동
        btn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 0) 초기 상태: 주황 그라데이션 + 햄스터만 준비
        root.setBackgroundResource(R.drawable.gradient_bg)

        // 텍스트/버튼은 나중에 등장
        name.alpha = 0f
        sub.alpha  = 0f
        btn.alpha  = 0f

        // 햄스터는 중앙에서 살짝 스케일 업되며 등장
        pill.alpha = 0f
        pill.scaleX = 0.9f
        pill.scaleY = 0.9f

        pill.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600L)      // 중앙에 “슥” 나타나는 시간
            .setStartDelay(200L)    // 앱 켜지고 잠깐 텀
            .withEndAction {
                // 중앙에서 잠깐 머무름
                pill.postDelayed({
                    // 1) 배경을 흰색으로 전환 + 이미지 opp로 변경
                    animateBackgroundToWhite(root, pill) {
                        // 2) 배경 전환 후 햄스터 위로 이동 + 텍스트/버튼 등장
                        animatePillUpAndShowTexts(pill, name, sub, btn)
                    }
                }, 500L)
            }
            .start()
    }

    private fun animateBackgroundToWhite(
        root: ConstraintLayout,
        pill: ImageView,
        onEnd: () -> Unit
    ) {
        // 페이드 아웃 → 배경/이미지 변경 → 페이드 인
        root.animate()
            .alpha(0f)
            .setDuration(200L)
            .withEndAction {
                // 배경 흰색으로
                root.setBackgroundColor(Color.WHITE)
                // 햄스터 이미지 opp 버전으로 교체
                pill.setImageResource(R.drawable.welcome_hamster_opp)

                root.alpha = 0f
                root.animate()
                    .alpha(1f)
                    .setDuration(200L)
                    .withEndAction { onEnd() }
                    .start()
            }
            .start()
    }

    private fun animatePillUpAndShowTexts(
        pill: ImageView,
        name: TextView,
        sub: TextView,
        btn: MaterialButton
    ) {
        val duration = 500L
        val delayStep = 150L

        val originalY = pill.translationY

        // 햄스터를 더 위로 크게 올리기 (기존 -180f → -230f)
        pill.animate()
            .translationY(originalY - 230f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                // 텍스트/버튼을 아래에서 위로 + 페이드 인
                listOf(name, sub, btn).forEach {
                    it.alpha = 0f
                    it.translationY = 20f
                }

                name.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350L)
                    .setStartDelay(0L)
                    .start()

                sub.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350L)
                    .setStartDelay(delayStep)
                    .start()

                btn.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350L)
                    .setStartDelay(delayStep * 2)
                    .start()
            }
            .start()
    }
}
