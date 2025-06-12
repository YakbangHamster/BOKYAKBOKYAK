package com.example.yakbanghamster

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection


data class BottomBarMenuItem(val iconRes: Int, val label: String)

@Composable
fun BottomBarWithCenterFab(
    menuItems: List<BottomBarMenuItem>,
    selectedIndex: Int,
    onMenuClick: (Int) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fabSize = 70.dp
    val cutoutRadiusPx = with(density) { (fabSize / 2 + 1.dp).toPx() }
    val cornerRadiusPx = with(density) { 20.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 곡선 네비게이션 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(
                    BottomNavShape(
                        cornerRadius = cornerRadiusPx,
                        dockRadius = cutoutRadiusPx
                    )
                )
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    item = menuItems[0],
                    selected = selectedIndex == 0,
                    onClick = { onMenuClick(0) },
                    iconSize = 28.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 5.dp)
                )
                BottomBarItem(
                    item = menuItems[1],
                    selected = selectedIndex == 1,
                    onClick = { onMenuClick(1) },
                    iconSize = 28.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp, top = 5.dp)
                )
                Spacer(modifier = Modifier.width(fabSize + 16.dp))
                BottomBarItem(
                    item = menuItems[2],
                    selected = selectedIndex == 2,
                    onClick = { onMenuClick(2) },
                    iconSize = 28.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp, top = 5.dp)
                )
                BottomBarItem(
                    item = menuItems[3],
                    selected = selectedIndex == 3,
                    onClick = { onMenuClick(3) },
                    iconSize = 28.dp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 5.dp)
                )
            }
        }

        // 중앙 FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 15.dp)
                .size(fabSize)
                .clip(CircleShape)
                .background(Color(0xFFFF9800))
                .zIndex(1f)
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.pill),
                contentDescription = "FAB",
                tint = Color.White,
                modifier = Modifier.size(33.dp)
            )
        }
    }
}

@Composable
fun BottomBarItem(
    item: BottomBarMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {

    val targetScale = if (selected) 1.05f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = 600f, dampingRatio = 0.5f
        ), label = ""
    )

    Column(
        modifier = modifier
            .scale(scale)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.label,
            tint = if (selected) Color.Black else Color.Gray,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = item.label,
            color = if (selected) Color.Black else Color.Gray,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

class BottomNavShape(
    private val cornerRadius: Float,
    private val dockRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        path = Path().apply {
            moveTo(0f, cornerRadius)
            arcTo(
                rect = Rect(
                    Offset(0f, 0f),
                    Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width / 2 - dockRadius, 0f)
            arcTo(
                rect = Rect(
                    Offset(size.width / 2 - dockRadius, -dockRadius),
                    Size(dockRadius * 2, dockRadius * 2)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width - cornerRadius, 0f)
            arcTo(
                rect = Rect(
                    Offset(size.width - cornerRadius * 2, 0f),
                    Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    )
}
