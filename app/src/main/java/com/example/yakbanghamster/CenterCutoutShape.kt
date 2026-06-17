package com.example.yakbanghamster

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CenterCutoutShape(
    private val cutoutRadius: Float,
    private val cornerRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        path = Path().apply {
            val width = size.width
            val height = size.height
            val center = width / 2

            // 왼쪽 둥근 모서리
            moveTo(0f, cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(0f, 0f),
                    Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(center - cutoutRadius, 0f)

            // 중앙 파임(반원)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(center - cutoutRadius, -cutoutRadius),
                    Size(cutoutRadius * 2, cutoutRadius * 2)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(width - cornerRadius, 0f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(width - cornerRadius * 2, 0f),
                    Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
    )
}