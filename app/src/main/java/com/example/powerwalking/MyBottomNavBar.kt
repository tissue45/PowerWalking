// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.powerwalking.R

@Composable
fun MyBottomNavBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf("메인", "캐릭터", "아레나", "상점")
    val navBarHeight = 120.dp // 100dp -> 120dp로 키움

    // 시스템 내비게이션 바 높이만큼 패딩을 추가하기 위해 WindowInsets 사용
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = navBarInsets.calculateBottomPadding()

    // 하단바를 조금 더 내리기 위해 추가적인 오프셋을 적용 (예: 10.dp 아래로)
    val additionalOffset = 10.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
            .offset(y = additionalOffset) // 하단으로 조금 더 내림
            .height(navBarHeight)
            .graphicsLayer { clip = false }
    ) {
        items.forEachIndexed { index, item ->
            CustomAppNavigationBarItem(
                index = index,
                item = item,
                isSelected = selectedItem == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

@Composable
private fun RowScope.CustomAppNavigationBarItem(
    index: Int,
    item: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconId = when (index) {
        0 -> R.drawable.main
        1 -> R.drawable.cat
        2 -> R.drawable.arena
        else -> R.drawable.shop
    }

    val targetScale = if (isSelected) 1.2f else 1.0f
    val scale by animateFloatAsState(targetValue = targetScale, label = "icon-scale-animation")

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer { clip = false }
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 잔디 배경 이미지도 높이에 맞춰 비율 조정
        Image(
            painter = painterResource(id = R.drawable.grass),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp) // 150dp -> 180dp로 키움
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(100.dp) // 80dp -> 100dp로 키움
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Icon(
            painter = painterResource(id = iconId),
            contentDescription = item,
            tint = Color.Unspecified,
            modifier = (if (index == 1) {
                Modifier
                    .size(80.dp) // 65dp -> 80dp로 키움
                    .offset(y = -8.dp)
            } else {
                Modifier.size(90.dp) // 75dp -> 90dp로 키움
            }).scale(scale)
        )
    }
}
