package com.example.yakbanghamster

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.yakbanghamster.yolo.DetectionResult

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val detections = mutableListOf<DetectionResult>()

    private val classColors = mapOf(
        "pill" to Color.BLUE,
        "hand" to Color.GREEN,
        "cup"  to Color.RED
    )

    private val paintBox = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val paintTextBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 120
    }

    private val bgRect = RectF()
    private val textBuilder = StringBuilder()
    private val fontMetrics = Paint.FontMetrics()

    fun setResults(results: List<DetectionResult>) {
        detections.clear()
        detections.addAll(results)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paintText.getFontMetrics(fontMetrics)
        val textHeight = fontMetrics.bottom - fontMetrics.top

        for (d in detections) {
            val color = classColors[d.label] ?: Color.YELLOW
            paintBox.color = color
            paintTextBg.color = color

            // 박스
            canvas.drawRect(d.rect, paintBox)

            // 텍스트
            textBuilder.clear()
            textBuilder.append(d.label).append(" ").append("%.2f".format(d.confidence))
            val text = textBuilder.toString()
            val textWidth = paintText.measureText(text)

            bgRect.set(
                d.rect.left,
                d.rect.top - textHeight - 10,
                d.rect.left + textWidth + 10,
                d.rect.top
            )
            canvas.drawRoundRect(bgRect, 8f, 8f, paintTextBg)
            canvas.drawText(text, d.rect.left + 5, d.rect.top - 10, paintText)
        }
    }
}
