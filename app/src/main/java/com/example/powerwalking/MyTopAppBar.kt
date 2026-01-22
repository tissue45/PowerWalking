// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(totalCoins: Int) {
    var showMailPopup by remember { mutableStateOf(false) }

    if (showMailPopup) {
        AlertDialog(
            onDismissRequest = { showMailPopup = false },
            title = {
                Text(text = "우편함", fontFamily = memomentkkukkukk)
            },
            text = {
                Text(
                    text = "받은 우편이 없습니다.",
                    fontFamily = memomentkkukkukk,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(onClick = { showMailPopup = false }) {
                    Text(text = "닫기", fontFamily = memomentkkukkukk)
                }
            }
        )
    }

    // 사용자의 요청대로 상단바 높이를 줄임 (100dp -> 70dp 정도로 복구/조정)
    // padding(top = 20.dp) 등도 제거하거나 줄여서 컴팩트하게 만듭니다.

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp) // 100dp -> 70dp로 줄임
    ) {
        TopAppBar(
            modifier = Modifier.height(70.dp), // TopAppBar 높이도 줄임
            title = {},
            navigationIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(top = 8.dp) // 패딩 줄임 (20dp -> 8dp)
                ) {
                    Box(
                        modifier = Modifier.size(50.dp) // 프로필 사진 크기 줄임 (70dp -> 50dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cat_foot),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Text(
                        text = "파워킹유저",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 22.sp,
                        fontFamily = memomentkkukkukk
                    )
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp) // 패딩 줄임 (20dp -> 8dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.LightGray, shape = RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 6.dp) // 패딩 줄임
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.coin),
                            contentDescription = "Coin",
                            modifier = Modifier.size(30.dp) // 코인 아이콘 크기 줄임 (40dp -> 30dp)
                        )
                        Text(
                            text = NumberFormat.getNumberInstance(Locale.US).format(totalCoins),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 16.sp // 글씨 크기 줄임 (18sp -> 16sp)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.mailbox),
                        contentDescription = "Mailbox",
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .size(50.dp) // 우편함 아이콘 크기 줄임 (70dp -> 50dp)
                            .clickable { showMailPopup = true }
                    )
                }
            }
        )
    }
}
